package com.team.xslides.web;

import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;

import com.team.xslides.domain.Presentation;
import com.team.xslides.service.PresentationService;
import com.team.xslides.service.TagService;
import com.team.xslides.service.TemplateService;
import com.team.xslides.service.UserService;
import com.team.xslides.domain.User;
import com.team.xslides.domain.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PresentationController {
    @Autowired
    private PresentationService presentationService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TemplateService templateService;

    @RequestMapping(value = "/createPresentation", method = RequestMethod.POST)
    public ModelAndView createPresentation(HttpServletRequest request, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return new ModelAndView("redirect:/accessDenied");
        }
        Presentation presentation = new Presentation();
        presentation.setAuthor(((User) session.getAttribute("user")));
        presentation.setTitle(request.getParameter("title"));
        presentation.setContent("");
        presentation.setJson("");
        presentation.setTheme(request.getParameter("theme"));
        presentation.setDescription(request.getParameter("description"));
        List<String> names = Arrays.asList(request.getParameter("tags").split(","));
        Set<Tag> tags = new HashSet<Tag>();
        for (String name : names) {
            Tag tag = new Tag();
            tag.setName(name);
            tags.add(tag);
        }
        presentation.setTags(tags);
        session.setAttribute("currentPresentation", presentation);
        
        Integer templateID = Integer.parseInt(request.getParameter("template"));
        presentation.setTemplate(templateService.getTemplateById(templateID));
        presentationService.addPresentation(presentation);
        //presentationService.setTemplate(presentation.getId(), Integer.parseInt(request.getParameter("template")));
        ModelAndView mv = new ModelAndView("editor");
        mv.addObject("id", presentation.getId());
        return mv;
    }

    @RequestMapping(value = "/createPresentation")
    public ModelAndView createPresentation(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return new ModelAndView("redirect:/accessDenied");
        }
        return new ModelAndView("create_presentation");
    }

    @RequestMapping(value = "/showcurrent")
    public String show() {
        return "index";
    }

    @RequestMapping(value = "/saveCurrentPresentation", method = RequestMethod.POST)
    public ModelAndView saveCurrentPresentation(HttpSession session, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("message");
        if (session.getAttribute("user") == null) {
            mv.addObject("message", "Login, please");
            return mv;
        }
        Presentation presentation = presentationService.getPresentation(Integer.parseInt(request.getParameter("id")));
        if (!presentation.getAuthor().getId().equals(((User) session.getAttribute("user")).getId())) {
            System.out.println(presentation.getAuthor().getId().toString() + " "
                            + ((User) session.getAttribute("user")).getId().toString());
            mv.addObject("message", "It's not your presentation");
            return mv;
        }
        presentation.setContent(request.getParameter("content"));
        presentation.setJson(request.getParameter("json"));
        presentationService.addPresentation(presentation);
        mv.addObject("message", "Save ok");
        return mv;
    }

    @RequestMapping(value = "/viewPresentation/{presentationId}")
    public String viewPresentation(@PathVariable("presentationId") String presentationId, Map<String, Object> map) {
        try {
            Integer id = Integer.parseInt(presentationId);
            map.put("html", presentationService.getContent(id));
            return "viewPresentation";
        } catch(NumberFormatException exception) {
            return "viewPresentation";
        }
    }

    @RequestMapping(value = "/getPresentationJSON/{presentationId}")
    public String getPresentationJSON(@PathVariable("presentationId") String presentationId, Map<String, Object> map) {
        Integer id = Integer.parseInt(presentationId);
        map.put("html", presentationService.getPresentationJson(id));
        return "viewPresentation";
    }

    @RequestMapping(value = "/userPresentations/{id}", method = RequestMethod.GET)
    public ModelAndView userPresentations(@PathVariable("id") Integer id, HttpSession session) {
        session.setAttribute("presentationsList", userService.getUserPresentations(id));
        session.setAttribute("author", userService.getUser(id));
        return new ModelAndView("redirect:/userPresentations");
    }

    @RequestMapping(value = "/userPresentations", method = RequestMethod.GET)
    public ModelAndView userPresentations(HttpSession session) {
        ModelAndView mv = new ModelAndView("user_presentations");
        mv.addObject("presentationsList", session.getAttribute("presentationsList"));
        mv.addObject("author", session.getAttribute("author"));
        session.removeAttribute("presentationsList");
        session.removeAttribute("author");
        return mv;
    }

    @RequestMapping(value = "/deletePresentation/{Id}", method = RequestMethod.POST)
    public ModelAndView deletePresentation(@PathVariable("Id") Integer id, HttpSession session) {
        User user;
        if ((user = (User) session.getAttribute("user")) == null || !user.getId().equals(getAuthorId(id))) {
            return new ModelAndView("redirect:/accessDenied");
        } else {
            presentationService.removePresentation(id);
            return new ModelAndView("redirect:/userPresentations/" + user.getId());
        }
    }

    @RequestMapping(value = "/byTag", method = RequestMethod.GET)
    public ModelAndView byTag(HttpServletRequest request, HttpSession session) throws UnsupportedEncodingException {
        ModelAndView mv = new ModelAndView("search_result");
        String name = (URLDecoder.decode(request.getParameter("tagName"), "UTF-8"));
        mv.addObject("presentationsList", tagService.getPresentations(name));
        return mv;
    }

    @RequestMapping(value = "/newTitle", method = RequestMethod.POST)
    public ModelAndView newTitle(HttpServletRequest request, HttpSession session) {
        Integer id = Integer.parseInt(request.getParameter("titleId"));
        if (!getAuthorId(id).equals(((User)session.getAttribute("user")).getId())) {
            return new ModelAndView("redirect:/accessDenied");
        }
        presentationService.setNewTitle(id, request.getParameter("title"));
        return new ModelAndView("redirect:/userPresentations/" + getAuthorId(id));
    }
    
    @RequestMapping(value = "/newTheme", method = RequestMethod.POST)
    public ModelAndView newTheme(HttpServletRequest request, HttpSession session) {
        Integer id = Integer.parseInt(request.getParameter("themeId"));
        if (!getAuthorId(id).equals(((User)session.getAttribute("user")).getId())) {
            return new ModelAndView("redirect:/accessDenied");
        }
        presentationService.setNewTheme(id, request.getParameter("theme"));
        return new ModelAndView("redirect:/userPresentations/" + getAuthorId(id));
    }
    
    @RequestMapping(value = "/newDescription", method = RequestMethod.POST)
    public ModelAndView newDescription(HttpServletRequest request, HttpSession session) {
        Integer id = Integer.parseInt(request.getParameter("descriptionId"));
        if (!getAuthorId(id).equals(((User)session.getAttribute("user")).getId())) {
            return new ModelAndView("redirect:/accessDenied");
        }
        presentationService.setNewDescription(id, request.getParameter("description"));
        return new ModelAndView("redirect:/userPresentations/" + getAuthorId(id));
    }
    
    @RequestMapping(value = "/newTags", method = RequestMethod.POST)
    public ModelAndView newTags(HttpServletRequest request, HttpSession session) {
        Integer id = Integer.parseInt(request.getParameter("tagsId"));
        if (!getAuthorId(id).equals(((User)session.getAttribute("user")).getId())) {
            return new ModelAndView("redirect:/accessDenied");
        }
        List<String> names = Arrays.asList(request.getParameter("tags").split(","));
        Set<Tag> tags = new HashSet<Tag>();
        for (String name : names) {
            Tag tag = new Tag();
            tag.setName(name);
            tags.add(tag);
        }
        presentationService.clearTags(id);
        presentationService.setNewTags(id, tags);
        return new ModelAndView("redirect:/userPresentations/" + getAuthorId(id));
    }

    private Integer getAuthorId(Integer id) {
        return presentationService.getPresentation(id).getAuthor().getId();
    }

    @RequestMapping(value = "/getVideoUrl")
    public ModelAndView getYoutubeVideoDirectLink( HttpServletRequest request ) throws UnsupportedEncodingException {
	ModelAndView mv = new ModelAndView("message");
	String url = request.getParameter("url");
	Youtube video = new Youtube( url );
	mv.addObject("message",video.getLinks().values().toArray()[1]);
	return mv;
    }
    
    @RequestMapping(value = "/getPresentationTemplate/{Id}", method = RequestMethod.GET)
    public ModelAndView getPresentationTemplate(@PathVariable("Id") Integer id, HttpSession session) {
        ModelAndView mv = new ModelAndView("message");
        mv.addObject("message", presentationService.getTemplate(id));
        return mv;
    }
}