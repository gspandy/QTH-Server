package com.nowbook.image.exception;

/**
 * Author:  <a href="mailto:jl@nowbook.com">jl</a>
 * Date: 2013-07-27
 */
public class ImageUploadException extends Exception {
    private static final long serialVersionUID = 6295717443044899527L;

    public ImageUploadException() {
    }

    public ImageUploadException(String message) {
        super(message);
    }

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageUploadException(Throwable cause) {
        super(cause);
    }
}
