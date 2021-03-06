package com.team.xslides.web;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.team.xslides.domain.User;
import com.team.xslides.service.UserService;

@Controller
public class ProfileController {
    
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/settings", method = RequestMethod.GET)
    public ModelAndView settings() {
        return new ModelAndView("settings");
    }
    
    @RequestMapping(value = "/newName", method = RequestMethod.POST)
    public ModelAndView newName(HttpServletRequest request, HttpSession session) {
        User user;
        String displayname = null;
        try {
            displayname = new String(request.getParameter("displayname").getBytes("ISO-8859-1"),"utf8");
        } catch(UnsupportedEncodingException ex) {
            System.out.println("bad encode");
        }
        ModelAndView mv = new ModelAndView("settings");
        if ((user = (User) session.getAttribute("user")) == null) {
            mv.setViewName("redirect:/accessDenied");
        } else {
            if ((user = userService.getUser(user.getEmail(), request.getParameter("password"))) == null) {
                mv.addObject("errorPassword", true);
            } else {
                userService.setNewDisplayname(user.getId(), displayname);
                session.setAttribute("user", userService.getUser(user.getId()));
                mv.addObject("nameChanged", true);
            }
        }
        return mv;
    }
    
    @RequestMapping(value = "/newPassword", method = RequestMethod.POST)
    public ModelAndView newPassword(HttpServletRequest request, HttpSession session) {
        User user;
        ModelAndView mv = new ModelAndView("settings");
        if ((user = (User) session.getAttribute("user")) == null) {
            mv.setViewName("redirect:/accessDenied");
        } else {
            if ((user = userService.getUser(user.getEmail(), request.getParameter("password"))) == null) {
                mv.addObject("errorPassword", true);
            } else {
                userService.setNewPassword(user.getId(), request.getParameter("newPassword"));
                session.setAttribute("user", userService.getUser(user.getId()));
                mv.addObject("passwordChanged", true);
            }
        }
        return mv;
    }
}
