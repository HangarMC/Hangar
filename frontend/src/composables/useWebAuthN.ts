import { useInternalApi } from "~/composables/useApi";

const lookup = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
const reverseLookup = new Uint8Array(256);

for (let i = 0; i < lookup.length; i++) {
  reverseLookup[lookup.charCodeAt(i)] = i;
}

export function decodeBase64Url(base64url: string) {
  const base64urlLength = base64url.length;

  const placeHolderLength = base64url.charAt(base64urlLength - 2) === "=" ? 2 : base64url.charAt(base64urlLength - 1) === "=" ? 1 : 0;
  const bufferLength = (base64urlLength * 3) / 4 - placeHolderLength;

  const arrayBuffer = new ArrayBuffer(bufferLength);
  const uint8Array = new Uint8Array(arrayBuffer);

  let j = 0;
  for (let i = 0; i < base64urlLength; i += 4) {
    const tmp0 = reverseLookup[base64url.charCodeAt(i)];
    const tmp1 = reverseLookup[base64url.charCodeAt(i + 1)];
    const tmp2 = reverseLookup[base64url.charCodeAt(i + 2)];
    const tmp3 = reverseLookup[base64url.charCodeAt(i + 3)];

    uint8Array[j++] = (tmp0 << 2) | (tmp1 >> 4);
    uint8Array[j++] = ((tmp1 & 15) << 4) | (tmp2 >> 2);
    uint8Array[j++] = ((tmp2 & 3) << 6) | (tmp3 & 63);
  }

  return arrayBuffer;
}

export function encodeBase64Url(arrayBuffer: ArrayBuffer) {
  const uint8Array = new Uint8Array(arrayBuffer);
  const length = uint8Array.length;
  let base64url = "";

  for (let i = 0; i < length; i += 3) {
    base64url += lookup[uint8Array[i] >> 2];
    base64url += lookup[((uint8Array[i] & 3) << 4) | (uint8Array[i + 1] >> 4)];
    base64url += lookup[((uint8Array[i + 1] & 15) << 2) | (uint8Array[i + 2] >> 6)];
    base64url += lookup[uint8Array[i + 2] & 63];
  }

  if (length % 3 === 1) {
    base64url = base64url.substring(0, base64url.length - 2);
  } else if (length % 3 === 2) {
    base64url = base64url.substring(0, base64url.length - 1);
  }
  return base64url;
}

export async function getAttestationOptions() {
  const response = await useInternalApi<PublicKeyCredentialCreationOptions>("webauthn/attestation/options");
  console.log("response", response);

  // TODO error handling

  return {
    rp: response.rp,
    user: response.user
      ? {
          id: decodeBase64Url(response.user.id as unknown as string),
          name: response.user.name,
          displayName: response.user.displayName,
        }
      : null,
    challenge: decodeBase64Url(response.challenge as unknown as string),
    pubKeyCredParams: response.pubKeyCredParams,
    timeout: response.timeout,
    excludeCredentials: response.excludeCredentials
      ? response.excludeCredentials.map((credential) => {
          return {
            type: credential.type,
            id: decodeBase64Url(credential.id as unknown as string),
            transports: credential.transports,
          };
        })
      : null,
    authenticatorSelection: response.authenticatorSelection,
    attestation: response.attestation,
    extensions: response.extensions,
  } as PublicKeyCredentialCreationOptions;
}

export async function getAssertionOptions() {
  const response = await useInternalApi<PublicKeyCredentialRequestOptions>("webauthn/assertion/options");
  console.log("response", response);

  // TODO error handling

  return {
    challenge: decodeBase64Url(response.challenge as unknown as string),
    timeout: response.timeout,
    rpId: response.rpId,
    allowCredentials: response.allowCredentials
      ? response.allowCredentials.map((credential) => {
          return {
            type: credential.type,
            id: decodeBase64Url(credential.id as unknown as string),
            transports: credential.transports,
          };
        })
      : null,
    userVerification: response.userVerification,
    extensions: response.extensions,
  } as PublicKeyCredentialRequestOptions;
}
