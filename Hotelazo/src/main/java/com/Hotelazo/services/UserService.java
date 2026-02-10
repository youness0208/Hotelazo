package com.Hotelazo.services;

import com.Hotelazo.dtos.LoginRequest;
import com.Hotelazo.dtos.RegistrationRequest;
import com.Hotelazo.dtos.Response;
import com.Hotelazo.dtos.UserDTO;
import com.Hotelazo.entities.User;

public interface UserService {

    Response registerUser(RegistrationRequest registrationRequest);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    Response getOwnAccountDetails();
    User getCurrentLoggedInUser();
    Response updateOwnAccount(UserDTO userDTO);
    Response deleteOwnAccount();
    Response getMyBookingHistory();
}
