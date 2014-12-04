package com.ss.atmlocator.controller;

import com.ss.atmlocator.entity.User;
import com.ss.atmlocator.service.UserService;
import com.ss.atmlocator.utils.*;
import com.ss.atmlocator.validator.UserProfileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Class for processing requests from adminUsers page
 */

@Controller
public class AdminUsersController {

    @Autowired
    UserService userService;

    @Autowired
    UserDetailsManager userDetailsManager;

    @Autowired
    UserProfileValidator validationService;

    @Autowired
    @Qualifier("mail")
    private SendMails sendMails;

    @Autowired
    @Qualifier("emailcreator")
    EmailCreator emailCreator;

    @Autowired
    private MessageSource messages;


    public static final String EMAIL_SUBJECT = "Change user credentials";

    @RequestMapping(value = "/findUser", method = RequestMethod.GET)
    @ResponseBody
    public User findUser(@RequestParam(Constants.FIND_BY) String findBy,
                         @RequestParam(Constants.FIND_VALUE) String findValue) {
        try {
            if (Constants.USER_LOGIN.equals(findBy)) {
                return userService.getUserByName(findValue);
            } else {
                return userService.getUserByEmail(findValue);
            }
        } catch (PersistenceException pe) {
            //if user not found
            User userNotFound = new User();
            userNotFound.setId(-1);
            return userNotFound;
        }
    }

    @RequestMapping(value = "/adminUsers", method = RequestMethod.GET)
    public String adminUsers(ModelMap model) {
        model.addAttribute("active", "adminUsers");
        return "adminUsers";
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.DELETE)
    @ResponseBody
    public OutResponse deleteUser(@RequestParam(Constants.USER_ID) int id) {
        //variables for sending response about result of operation
        OutResponse response = new OutResponse();
        List<ErrorMessage> errorMessageList = new ArrayList<ErrorMessage>(1);
        response.setErrorMessageList(errorMessageList);

        //id of user who want to delete
        int currentLoggedUserId =  userService.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
        //Check user want to remove himself
        if (id == currentLoggedUserId) {
            return new OutResponse(Constants.INFO, new ErrorMessage(Constants.DELETE,
                    messages.getMessage("user.removing_yourself", null, Locale.ENGLISH)));
        };

        try {
            userService.deleteUser(id);

            return new OutResponse(Constants.SUCCESS, new ErrorMessage(Constants.DELETE,
                    messages.getMessage("operation.success", null, Locale.ENGLISH)));

        } catch (PersistenceException pe) {
            return new OutResponse(Constants.ERROR, new ErrorMessage(Constants.DELETE,
                    messages.getMessage("operation.error", null, Locale.ENGLISH)));
        }
    }

    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    @ResponseBody
    public OutResponse updateUser(@RequestParam(value=Constants.USER_ID) int id,
                                  @RequestParam(value=Constants.USER_LOGIN) String newLogin,
                                  @RequestParam(value=Constants.USER_EMAIL) String newEmail,
                                  @RequestParam(value=Constants.USER_PASSWORD, required = false) String newPassword,
                                  @RequestParam(value=Constants.USER_ENABLED) int enabled){

        //Creating user from request parameters
        User updatedUser = new User(id, newLogin, newEmail, newPassword, enabled);

        //checking if nothing to update
        if (userService.isNotModified(updatedUser)) {
            return new OutResponse(Constants.INFO, new ErrorMessage(Constants.UPDATE,
                    messages.getMessage("user.nothing_to_update", null, Locale.ENGLISH)));
        }

        //validating user profile
        MapBindingResult errors = new MapBindingResult(new HashMap<String, String>(), User.class.getName());
        validationService.validate(updatedUser, null, errors);

        if (errors.hasErrors()) {//if validation unsuccessful add all errors to response
            OutResponse response = new OutResponse(Constants.ERROR,null);
            for (FieldError error : errors.getFieldErrors()) {
                response.getErrorMessageList().add(new ErrorMessage(error.getField(), error.getCode()));
            }
            return response;
        };

        try {
             //try to update user in database
            userService.editUser(updatedUser);

            //try to send e-mail about changes to user
            //if password was changed send message with new password
            sendMails.sendMail(updatedUser.getEmail(), EMAIL_SUBJECT, emailCreator.create(Constants.UPDATE_TEMPLATE, updatedUser));

            //id of user who is logged
            int currentLoggedUserId =  userService.getUserByName(SecurityContextHolder.getContext().getAuthentication().getName()).getId();
            //relogin if change yourself
            if (updatedUser.getId() == currentLoggedUserId) {
                userService.doAutoLogin(updatedUser.getLogin());
            };

            //Return SUCCESS
            return new OutResponse(Constants.SUCCESS, new ErrorMessage(Constants.UPDATE,
                    messages.getMessage("operation.success", null, Locale.ENGLISH)));
        }catch (PersistenceException pe){
            //Return PERSISTENCE_ERROR
            return new OutResponse(Constants.ERROR, new ErrorMessage(Constants.UPDATE,
                    messages.getMessage("operation.error", null, Locale.ENGLISH)));
        } catch(MailException me){
            //Return EMAIL_ERROR
            return new OutResponse(Constants.ERROR, new ErrorMessage(Constants.SEND_EMAIL,
                    messages.getMessage("email.error", null, Locale.ENGLISH)));
        }
    }
}
