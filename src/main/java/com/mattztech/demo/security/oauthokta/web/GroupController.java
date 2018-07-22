package com.mattztech.demo.security.oauthokta.web;

import com.mattztech.demo.security.oauthokta.model.Group;
import com.mattztech.demo.security.oauthokta.model.User;
import com.mattztech.demo.security.oauthokta.model.repository.GroupRepository;
import com.mattztech.demo.security.oauthokta.model.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class GroupController {

  private final Logger log = LoggerFactory.getLogger(GroupController.class);
  private GroupRepository groupRepository;
  private UserRepository userRepository;

  public GroupController(final GroupRepository groupRepository, final UserRepository userRepository) {
    this.groupRepository = groupRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/groups")
  Collection<Group> groups(Principal principal) {
    return groupRepository.findAllByUserId(principal.getName());
  }

  @GetMapping("/group/{id}")
  ResponseEntity<?> getGroup(@PathVariable Long id) {
    Optional<Group> group = groupRepository.findById(id);
    return group.map(response -> ResponseEntity.ok().body(response))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @PostMapping("/group")
  ResponseEntity<Group> createGroup(@Valid @RequestBody Group group,
      @AuthenticationPrincipal OAuth2User principal) throws URISyntaxException {
    log.info("Request to create group: {}", group);
    Map<String, Object> details = principal.getAttributes();
    String userId = details.get("sub").toString();

    // check to see if user already exists
    Optional<User> user = userRepository.findById(userId);
    group.setUser(user.orElse(new User(userId,
        details.get("name").toString(), details.get("email").toString())));

    Group result = groupRepository.save(group);
    return ResponseEntity.created(new URI("/api/group/" + result.getId()))
        .body(result);
  }

  @PutMapping("/group")
  ResponseEntity<Group> updateGroup(@Valid @RequestBody Group group) {
    log.info("Request to update group: {}", group);
    Group result = groupRepository.save(group);
    return ResponseEntity.ok().body(result);
  }

  @DeleteMapping("/group/{id}")
  public ResponseEntity<?> deleteGroup(@PathVariable Long id) {
    log.info("Request to delete group: {}", id);
    groupRepository.deleteById(id);
    return ResponseEntity.ok().build();
  }
}