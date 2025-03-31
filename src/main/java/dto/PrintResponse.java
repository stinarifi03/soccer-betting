package dto;

public class PrintResponse {
    private String pdfUrl;
    private String ticketCode;
    private String printStatus;

    public PrintResponse(String pdfUrl, String ticketCode) {
        this.pdfUrl = pdfUrl;
        this.ticketCode = ticketCode;
        this.printStatus = "READY";
    }

    // Getters
    public String getPdfUrl() { return pdfUrl; }
    public String getTicketCode() { return ticketCode; }
    public String getPrintStatus() { return printStatus; }
}