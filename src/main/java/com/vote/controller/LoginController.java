package com.vote.controller;

import com.vote.entity.Person;
import com.vote.entity.Secretary;
import com.vote.entity.User;
import com.vote.entity.VoteProject;
import com.vote.service.*;
import com.vote.util.SessionListener;
import org.aspectj.apache.bcel.verifier.VerifierFactoryObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * login控制层
 * Created by sunwe on 2018/3/8.
 */
@Controller
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private SecretaryService secretaryService;

    @Autowired
    private VoteProjectService voteProjectService;

    @Autowired
    private SessionService sessionService;

    /**
     * 登录
     * @param name
     * @param password
     * @param identity
     * @param model
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/login",method = {RequestMethod.POST})
    public String login(String name, String password, String identity, Model model, HttpServletRequest request, HttpServletResponse response) {

        int flag1= 0;
        if (identity.equals("user")) {
            flag1 = userService.findUserByNameAndPwd(name, password);
        } else if (identity.equals("manager")) {
            flag1 = managerService.findManagerByNameAndPwd(name, password);
        } else {
            flag1 = secretaryService.findManagerByNameAndPwd(name, password);
        }
        if (flag1 == 0) {
            model.addAttribute("msg", "*用户名或密码错误*");
            return "login";
        } else if (flag1 == 1) {
            request.getSession().setAttribute("username", name);
            request.getSession().setAttribute("identity",identity);
            if(identity.equals("user")){
                Person person = userService.selectUserByName(name);
                request.getSession().setAttribute("person",person);
                sessionService.changeLogin4Me(person);
                return "user/userframe";
            }else if(identity.equals("manager")){
                Person person = managerService.selectManagerByName(name);
                request.getSession().setAttribute("person",person);
                sessionService.changeLogin4Me(person);
                return "manager/managerframe";
            }else {
                Person person = secretaryService.selectSecretaryByName(name);
                request.getSession().setAttribute("person",person);
                sessionService.changeLogin4Me(person);
                return "secretary/secretaryframe";
            }
        }
        return "login";
    }

    /**
     * 注销登录
     * @param request
     * @param response
     * @param model
     * @param msg
     */
    @RequestMapping("/loginOut")
    public void loginOut(HttpServletRequest request,HttpServletResponse response,Model model,String msg){
        Person person = (Person) request.getSession().getAttribute("person");
        request.getSession().removeAttribute("username");
        request.getSession().removeAttribute("identity");
        request.getSession().removeAttribute("person");
        SessionListener.removeSession(request.getSession().getId());
        SessionListener.removeUserSession(String.valueOf(person.getId()));
        if(msg==null)
            msg="";
        try {
            response.getWriter().write("<script>window.top.location.href=\"/jsp/login.jsp?msg="+msg+"\"</script>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @RequestMapping("/toLogin")
    public String toLogin(Model model){
        model.addAttribute("msg","用户已在别处登录，您已被迫下线！");
        return "login";
    }
    @RequestMapping("/noLogin2Login")
    public String noLogin2Login(Model model){
        model.addAttribute("msg","用户未登录！请重新登录");
        return "login";
    }
}