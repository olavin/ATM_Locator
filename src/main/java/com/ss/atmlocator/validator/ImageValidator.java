package com.ss.atmlocator.validator;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageValidator implements Validator {

    private final int MAX_FILE_SIZE = 716800; //700kb

    @Autowired
    private MessageSource messages;

    @Override
    public boolean supports(Class<?> Clazz) {
        return MultipartFile.class.isAssignableFrom(Clazz);
    }

    @Override
    public void validate(Object object, Errors errors) {
        MultipartFile image = (MultipartFile) object;
        if (null == image) return;
        if (image.getSize() > MAX_FILE_SIZE) {
            errors.rejectValue("avatar", ValidationMessages.LIMIT_FILE_SIZE);
            return;
        } else if (!FilenameUtils.getExtension(image.getOriginalFilename()).equals("jpg") &&
                !FilenameUtils.getExtension(image.getOriginalFilename()).equals("jpeg") &&
                !FilenameUtils.getExtension(image.getOriginalFilename()).equals("png")) {
            errors.rejectValue("avatar", ValidationMessages.INVALID_FILE_EXTENSION);
            return;
        }
    }
}
