package controller;

import domain.Match;
import dto.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import service.TicketService;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/matches")
    public ResponseEntity<MatchResponse> addMatch(@Valid @RequestBody MatchRequest matchRequest) {
        MatchResponse response = ticketService.addMatch(convertToMatch(matchRequest));
        return ResponseEntity.ok(response);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TicketResponse> generateTicket(@Valid @RequestBody TicketRequest request) {
        TicketResponse response = ticketService.generateTicket(enrichRequest(request));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateTicketImage(@Valid @RequestBody TicketRequest request)
            throws IOException {
        byte[] imageBytes = ticketService.generateTicketImage(enrichRequest(request));
        return createImageResponse(imageBytes, "ticket.png");
    }

    @PostMapping(value = "/print", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printTicket(@Valid @RequestBody TicketRequest request)
            throws IOException {
        byte[] pdfBytes = ticketService.generatePrintableTicket(enrichRequest(request));
        return createPdfResponse(pdfBytes, "ticket_"+request.getTicketCode()+".pdf");
    }

    @GetMapping(value = "/barcode", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBarcode(@RequestParam String text) {
        try {
            byte[] barcode = ticketService.generateBarcodeImage(text);
            return createImageResponse(barcode, "barcode.png");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/verify-images")
    public ResponseEntity<ImageVerificationResponse> verifyImages() {
        return ResponseEntity.ok(ticketService.verifyImages());
    }

    @PostMapping("/clear")
    public ResponseEntity<ClearResponse> clearFields(@Valid @RequestBody ClearRequest request) {
        ticketService.clearFields(request.getFieldsToClear());
        return ResponseEntity.ok(new ClearResponse(
                request.getFieldsToClear(),
                "Fields cleared successfully"
        ));
    }

    // Helper methods
    private Match convertToMatch(MatchRequest request) {
        return new Match(
                request.getTeam1(),
                request.getTeam2(),
                request.getMatchDate(),
                request.getOdds(),
                request.getBetType(),
                request.getBetChosen(),
                request.getLeagueName(),
                request.getMatchId(),
                request.getCode(),
                request.getSport(),
                request.getDate(),
                request.getTime()
        );
    }

    private TicketRequest enrichRequest(TicketRequest request) {
        if (request.getTicketCode() == null || request.getTicketCode().isEmpty()) {
            request.setTicketCode(generateTicketCode());
        }
        return request;
    }

    private String generateTicketCode() {
        return "TKT-" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private ResponseEntity<byte[]> createImageResponse(byte[] imageData, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .header(HttpHeaders.EXPIRES, "0")
                .body(imageData);
    }

    private ResponseEntity<byte[]> createPdfResponse(byte[] pdfData, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(pdfData);
    }
}