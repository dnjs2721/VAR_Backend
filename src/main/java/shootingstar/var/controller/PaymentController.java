package shootingstar.var.controller;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shootingstar.var.Service.PaymentService;
import shootingstar.var.dto.req.ExchangeReqDto;
import shootingstar.var.dto.req.PaymentReqDto;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.jwt.JwtTokenProvider;

import java.io.IOException;

@Tag(name = "결제 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService paymentService;
    private final JwtTokenProvider jwtTokenProvider;
    private IamportClient iamportClient;

    @Value("${imp.api.key}")
    private String apiKey;

    @Value("${imp.api.secretkey}")
    private String secretKey;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    public IamportResponse<Payment> paymentLookup(String impUid) throws IamportResponseException, IOException {
        return iamportClient.paymentByImpUid(impUid);
    }

    @Operation(summary = "결제 검증 API", description = "결제 데이터를 PortOne서버의 데이터와 비교, 검증하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Payment.class)) }),
            //@ApiResponse(responseCode = "405", description = "Invalid input")
    })
    @PostMapping("/payment")
    public IamportResponse<Payment> paymentComplete(HttpServletRequest request, @RequestBody PaymentReqDto paymentReqDto) throws IamportResponseException, IOException {
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);

        Long amount = paymentReqDto.getPaymentAmount();

        IamportResponse<Payment> ires = paymentLookup(paymentReqDto.getImp_uid());

        paymentService.verifyIamportService(ires, amount, userUUID);

        return ires;
    }

    @PostMapping("/payment/exchange")
    public ResponseEntity<?> applyExchange(HttpServletRequest request, @RequestBody ExchangeReqDto exchangeReqDto) {
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        paymentService.applyExchange(exchangeReqDto, userUUID);
        return ResponseEntity.ok("포인트 환전 신청이 완료되었습니다.");
    }
}