declare module "hangar-api" {
  interface HangarException {
    message: string;
    messageArgs: any[];
    httpError: {
      statusCode: number;
      statusPhrase: string;
    };
  }

  interface HangarApiException extends HangarException {
    isHangarApiException: true;
    isMultiException: false;
  }

  interface MultiHangarApiException {
    isMultiException: true;
    isHangarApiException: true;
    exceptions: HangarApiException[];
  }

  interface ObjectError {
    code: string;
    errorMsg: string;
  }

  interface GlobalError extends ObjectError {
    objectName: string;
  }

  interface FieldError extends ObjectError {
    fieldName: string;
    rejectedValue: string;
  }

  interface HangarValidationException extends HangarException {
    isHangarValidationException: true;
    object: string;
    globalErrors: GlobalError[];
    fieldErrors: FieldError[];
  }
}
