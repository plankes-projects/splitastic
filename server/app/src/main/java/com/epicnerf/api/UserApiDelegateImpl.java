package com.epicnerf.api;

import com.epicnerf.hibernate.MapToOpenApiModel;
import com.epicnerf.hibernate.dao.GroupInviteDao;
import com.epicnerf.hibernate.dao.ImageDataDao;
import com.epicnerf.hibernate.model.GroupInvite;
import com.epicnerf.hibernate.model.ImageData;
import com.epicnerf.hibernate.repository.UserRepository;
import com.epicnerf.model.Invite;
import com.epicnerf.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserApiDelegateImpl implements UserApiDelegate {

    @Autowired
    private ImageDataDao imageDataDao;

    @Autowired
    private ApiSupport apiSupport;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupInviteDao groupInviteDao;

    @Autowired
    private MapToOpenApiModel openApiMapper;

    public ResponseEntity<User> userGet() {
        User u = openApiMapper.map(apiSupport.getCurrentUser());
        return ResponseEntity.ok(u);
    }

    public ResponseEntity<Void> userPut(User user) {
        com.epicnerf.hibernate.model.User u = apiSupport.getCurrentUser();
        u.setName(user.getName());

        if (user.getImage() != null && user.getImage().getUrl() != null) {
            ImageData imageData = imageDataDao.getImageDataFromUpload(user.getImage().getUrl());
            u.setImage(imageData);
        }

        userRepository.save(u);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<List<Invite>> userInviteGet() {
        com.epicnerf.hibernate.model.User user = apiSupport.getCurrentUser();
        List<GroupInvite> invites = groupInviteDao.getGroupInvites(user);
        return ResponseEntity.ok(openApiMapper.mapInvites(invites));
    }

    public ResponseEntity<Void> userResetApiKeyPut() {
        com.epicnerf.hibernate.model.User u = apiSupport.getCurrentUser();
        u.setToken(UUID.randomUUID().toString());
        userRepository.save(u);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
