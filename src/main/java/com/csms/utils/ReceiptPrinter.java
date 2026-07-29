package com.csms.utils;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

public final class ReceiptPrinter {

    private ReceiptPrinter() {
    }

    public static boolean printReceipt(
            String receiptContent) {
        if (receiptContent == null
                || receiptContent.isBlank()) {
            throw new IllegalArgumentException(
                    "Nội dung hóa đơn trống.");
        }

        PrinterJob printerJob = PrinterJob.getPrinterJob();

        printerJob.setJobName(
                "In hóa đơn CSMS");

        printerJob.setPrintable(
                new ReceiptPrintable(
                        receiptContent));

        if (!printerJob.printDialog()) {
            return false;
        }

        try {
            printerJob.print();
            return true;

        } catch (PrinterException exception) {
            throw new IllegalStateException(
                    "Không thể in hóa đơn: "
                            + exception.getMessage(),
                    exception);
        }
    }

    private static final class ReceiptPrintable
            implements Printable {

        private final String[] lines;

        private ReceiptPrintable(
                String receiptContent) {
            this.lines = receiptContent.split(
                    "\\R",
                    -1);
        }

        @Override
        public int print(
                Graphics graphics,
                PageFormat pageFormat,
                int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D graphics2D = (Graphics2D) graphics;

            graphics2D.translate(
                    pageFormat.getImageableX(),
                    pageFormat.getImageableY());

            Font font = new Font(
                    Font.MONOSPACED,
                    Font.PLAIN,
                    9);

            graphics2D.setFont(font);

            int lineHeight = graphics2D.getFontMetrics()
                    .getHeight();

            int y = lineHeight;

            for (String line : lines) {
                if (y > pageFormat.getImageableHeight()) {
                    break;
                }

                graphics2D.drawString(
                        line,
                        0,
                        y);

                y += lineHeight;
            }

            return PAGE_EXISTS;
        }
    }
}