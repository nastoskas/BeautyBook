package com.makeup.booking.service.impl;

import com.makeup.booking.repository.UserRepository;
import com.makeup.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
}
