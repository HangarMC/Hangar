declare module 'hangar-api' {
    interface HangarException {
        message: string;
        httpError: {
            statusCode: number;
            statusPhrase: string;
        };
    }

    interface HangarApiException extends HangarException {
        isHangarApiException: true;
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
