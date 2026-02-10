package com.Hotelazo.payments.stripe;



import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.Hotelazo.dtos.NotificationDTO;
import com.Hotelazo.entities.Booking;
import com.Hotelazo.entities.PaymentEntity;
import com.Hotelazo.enums.NotificationType;
import com.Hotelazo.enums.PaymentGateway;
import com.Hotelazo.enums.PaymentStatus;
import com.Hotelazo.exceptions.NotFoundException;
import com.Hotelazo.payments.stripe.dto.PaymentRequest;
import com.Hotelazo.repositories.BookingRepository;
import com.Hotelazo.repositories.PaymentRepository;
import com.Hotelazo.services.NotificationService;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Value("${stripe.api.secret.key}")
    private String secreteKey;


    public String createPaymentIntent(PaymentRequest paymentRequest){
        log.info("Inside createPaymentIntent()");
        Stripe.apiKey = secreteKey;
        String bookingReference = paymentRequest.getBookingReference();


        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking Not Found"));

        if (booking.getPaymentStatus() == PaymentStatus.COMPLETADO) {
            throw new NotFoundException("Payment already made for this booking");

        }

        try{
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(paymentRequest.getAmount().multiply(BigDecimal.valueOf(100)).longValue()) //amount cents
                    .setCurrency("EUR")
                    .putMetadata("bookingReference", bookingReference)
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return intent.getClientSecret();

        }catch (Exception e){
            throw new RuntimeException("Error creating payment intent");
        }

    }


    public void updatePaymentBooking(PaymentRequest paymentRequest){

        log.info("Inside updatePaymentBooking()");
        String bookingReference = paymentRequest.getBookingReference();

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(()-> new NotFoundException("Booing Not Found"));

        PaymentEntity payment = new PaymentEntity();
        payment.setPaymentGateway(PaymentGateway.STRIPE);
        payment.setAmount(paymentRequest.getAmount());
        payment.setTransactionId(paymentRequest.getTransactionId());
        boolean isSuccess = Boolean.TRUE.equals(paymentRequest.getSuccess());
payment.setPaymentStatus(isSuccess ? PaymentStatus.COMPLETADO : PaymentStatus.FALLIDO);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setBookingReference(bookingReference);
        payment.setUser(booking.getUser());

        if (!Boolean.TRUE.equals(paymentRequest.getSuccess())) {
            payment.setFailureReason(paymentRequest.getFailureReason());
        }

        paymentRepository.save(payment); //save payment to database

        //create and send notifiaction
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(booking.getUser().getEmail())
                .type(NotificationType.EMAIL)
                .bookingReference(bookingReference)
                .build();

        log.info("About to send notification inside updatePaymentBooking  by sms");


        if (paymentRequest.getSuccess()){
            booking.setPaymentStatus(PaymentStatus.COMPLETADO);
            bookingRepository.save(booking); //Update the booking

            notificationDTO.setSubject("Booking Payment Successful");
            notificationDTO.setBody("Congratulation!! Your payment for booking with reference: " + bookingReference + "is successful");
            notificationService.sendEmail(notificationDTO); //send email

        }else {

            booking.setPaymentStatus(PaymentStatus.FALLIDO);
            bookingRepository.save(booking); //Update the booking

            notificationDTO.setSubject("Booking Payment Failed");
            notificationDTO.setBody("Your payment for booking with reference: " + bookingReference + "failed with reason: " + paymentRequest.getFailureReason());
            notificationService.sendEmail(notificationDTO); //send email
        }





    }











}
