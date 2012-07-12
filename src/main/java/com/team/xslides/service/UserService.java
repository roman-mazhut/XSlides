package com.team.xslides.service;

import java.util.List;
import com.team.xslides.domain.User;

public interface UserService {

    public void addUser(User user);

    public List<User> listUser();
    
    public boolean hasUserWithEmail(String email);

    public void removeUser(Integer id);
}
