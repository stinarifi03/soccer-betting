package service;

import com.google.zxing.WriterException;
import domain.Bet;
import domain.Match;
import dto.ImageVerificationResponse;
import dto.MatchResponse;
import dto.TicketRequest;
import dto.TicketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import util.BarcodeGenerator;
import util.TicketGenerator6;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicketService {
    private final ResourceLoader resourceLoader;
    private static final String[] REQUIRED_IMAGES = {
            "goldbet_logo.png",
            "qr_code.png",
            "gioco.jpeg",
            "+18.jpg",
            "amd_timone.png",
            "adm.jpg",
            "barcode.png"
    };

    @Autowired
    public TicketService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ImageVerificationResponse verifyImages() {
        Map<String, Boolean> verificationResults = new HashMap<>();

        for (String image : REQUIRED_IMAGES) {
            verificationResults.put(image, checkImageExists(image));
        }

        return new ImageVerificationResponse(verificationResults);
    }

    private boolean checkImageExists(String imageName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:/static/images/" + imageName);
            return resource.exists() && resource.isReadable();
        } catch (Exception e) {
            return false;
        }
    }
    private final ConcurrentHashMap<String, Bet> activeBets = new ConcurrentHashMap<>();
    private final Map<String, Bet> ticketHistory = new ConcurrentHashMap<>();

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.host:localhost}")
    private String serverHost;

    public MatchResponse addMatch(Match match) {
        Bet currentBet = getOrCreateCurrentBet();
        currentBet.addMatch(match);
        return createMatchResponse(match, currentBet);
    }

    public TicketResponse generateTicket(TicketRequest request) {
        Bet bet = getCurrentBet();
        validateTicketRequest(request);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double totalWinnings = calculateTotalWinnings(bet, request.getBonusAmount());

        String ticketCode = generateTicketCode(request.getTicketCode());
        storeTicketInHistory(ticketCode, bet);

        return new TicketResponse(
                bet.getMatches(),
                bet.getBetAmount(),
                ticketCode,
                request.getLocation(),
                timestamp,
                request.getTg(),
                request.getDate(),
                request.getIb(),
                request.getNs(),
                request.getQuotaType(),
                request.getBetType(),
                request.getBetChosen(),
                bet.getTotalOdds(),
                request.getBonusAmount(),
                totalWinnings,
                generateBarcodeUrl(ticketCode),
                getImageUrls()
        );
    }

    public byte[] generateTicketImage(TicketRequest request) throws IOException {
        Bet bet = getCurrentBet();
        validateTicketRequest(request);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double totalWinnings = calculateTotalWinnings(bet, request.getBonusAmount());

        try {
            byte[] barcodeBytes = generateBarcodeImage(request.getTicketCode());
            BufferedImage barcodeImage = ImageIO.read(new ByteArrayInputStream(barcodeBytes));

            BufferedImage ticketImage = TicketGenerator6.generateTicket(
                    bet,
                    request.getTicketCode(),
                    request.getLocation(),
                    timestamp,
                    request.getTg(),
                    request.getDate(),
                    request.getIb(),
                    request.getNs(),
                    request.getQuotaType(),
                    request.getBetType(),
                    request.getBetChosen(),
                    bet.getTotalOdds(),
                    request.getBonusAmount(),
                    totalWinnings,
                    barcodeImage
            );

            return convertToPngBytes(ticketImage);
        } catch (Exception e) {
            throw new IOException("Failed to generate ticket image", e);
        }
    }

    public byte[] generatePrintableTicket(TicketRequest request) throws IOException {
        Bet bet = getCurrentBet();
        validateTicketRequest(request);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        double totalWinnings = calculateTotalWinnings(bet, request.getBonusAmount());

        return TicketGenerator6.generatePdfTicket(
                bet,
                request.getTicketCode(),
                request.getLocation(),
                timestamp,
                request.getTg(),
                request.getDate(),
                request.getIb(),
                request.getNs(),
                request.getQuotaType(),
                request.getBetType(),
                request.getBetChosen(),
                bet.getTotalOdds(),
                request.getBonusAmount(),
                totalWinnings
        );
    }

    public byte[] generateBarcodeImage(String text) throws IOException, WriterException {
        return BarcodeGenerator.generateBarcodeBytes(text, 260, 60);
    }

    public void clearFields(List<String> fieldsToClear) {
        Bet currentBet = getCurrentBet();

        if (fieldsToClear.contains("all")) {
            currentBet.setMatches(new ArrayList<>());
            currentBet.setBetAmount(0.0);
            activeBets.put("default", new Bet());
            return;
        }

        if (fieldsToClear.contains("matches")) currentBet.setMatches(new ArrayList<>());
        if (fieldsToClear.contains("betAmount")) currentBet.setBetAmount(0.0);
        if (fieldsToClear.contains("teams")) clearTeamFields(currentBet);
        if (fieldsToClear.contains("odds")) clearOdds(currentBet);
        if (fieldsToClear.contains("betType")) clearBetTypes(currentBet);
        if (fieldsToClear.contains("leagueInfo")) clearLeagueInfo(currentBet);

        currentBet.recalculate();
    }

    public Bet getCurrentBet() {
        return activeBets.computeIfAbsent("default", k -> new Bet());
    }

    public Optional<Bet> getHistoricalBet(String ticketCode) {
        return Optional.ofNullable(ticketHistory.get(ticketCode));
    }

    // Private helper methods
    private Bet getOrCreateCurrentBet() {
        return activeBets.computeIfAbsent("default", k -> new Bet());
    }

    private MatchResponse createMatchResponse(Match match, Bet bet) {
        return new MatchResponse(
                match,
                bet.getMatchCount(),
                bet.getTotalOdds(),
                bet.getPotentialWinnings()
        );
    }

    private String generateBarcodeUrl(String ticketCode) {
        return String.format("http://%s:%d/api/tickets/barcode?text=%s",
                serverHost,
                serverPort,
                URLEncoder.encode(ticketCode, StandardCharsets.UTF_8)
        );
    }

    private Map<String, String> getImageUrls() {
        String baseUrl = String.format("http://%s:%d/images/", serverHost, serverPort);
        return Map.of(
                "logo", baseUrl + "goldbet_logo.png",
                "qrCode", baseUrl + "qr_code.png",
                "gioco", baseUrl + "gioco.jpeg",
                "plus18", baseUrl + "+18.jpg",
                "amdTimone", baseUrl + "amd_timone.png",
                "adm", baseUrl + "adm.jpg",
                "barcode", baseUrl + "barcode.png"
        );
    }

    private static byte[] convertToPngBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private void validateTicketRequest(TicketRequest request) {
        if (request == null) throw new IllegalArgumentException("Ticket request cannot be null");
        if (request.getTicketCode() == null || request.getTicketCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket code is required");
        }
    }

    private double calculateTotalWinnings(Bet bet, double bonusAmount) {
        return bet.getPotentialWinnings() + (bonusAmount > 0 ? bonusAmount : 0);
    }

    private String generateTicketCode(String baseCode) {
        return baseCode + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void storeTicketInHistory(String ticketCode, Bet bet) {
        Bet historicalBet = new Bet(new ArrayList<>(bet.getMatches()), bet.getBetAmount());
        historicalBet.setBetId(ticketCode);
        ticketHistory.put(ticketCode, historicalBet);
    }

    private void clearTeamFields(Bet bet) {
        bet.getMatches().forEach(m -> {
            m.setTeam1("");
            m.setTeam2("");
        });
    }

    private void clearOdds(Bet bet) {
        bet.getMatches().forEach(m -> m.setOdds(0.0));
    }

    private void clearBetTypes(Bet bet) {
        bet.getMatches().forEach(m -> {
            m.setBetType("");
            m.setBetChosen("");
        });
    }

    private void clearLeagueInfo(Bet bet) {
        bet.getMatches().forEach(m -> {
            m.setLeagueName("");
            m.setMatchId("");
            m.setCode("");
        });
    }
}