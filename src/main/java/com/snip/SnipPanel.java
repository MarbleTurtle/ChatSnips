package com.snip;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import static net.runelite.client.RuneLite.SCREENSHOT_DIR;

public class SnipPanel extends PluginPanel {
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    JButton imageButton = new JButton("Generate Image");
    @Inject
    private Client client;
    private String First = "Full line of starting message.";
    private String Second = "Full line of ending message.";
    private String Error = "Please check that both lines match messages in chat box. Ranks/ Irons/ Mods/ Emojis are not detected [WIP].";
    private String Output = "Waiting to generate transcript.";
    private Boolean Ready = false;
    private String Transcript;
    private JTextArea firstBar;
    private JTextArea secondBar;
    private JTextArea OutputField = new JTextArea(Output);

    public SnipPanel(SnipConfig config, Client client) {
        this.client = client;
        // this may or may not qualify as a hack
        // but this lets the editor pane expand to fill the whole parent panel
        /*
        setBorder(new EmptyBorder(18, 10, 0, 10));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new GridBagLayout());
        */
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);

        firstBar = new JTextArea(First);
        firstBar.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        firstBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        firstBar.setLineWrap(true);
        firstBar.setWrapStyleWord(true);
        firstBar.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        firstBar.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (firstBar.getText().equals(First)) {
                    firstBar.setText("");
                    firstBar.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (firstBar.getText().isEmpty()) {
                    firstBar.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
                    firstBar.setText(First);
                }
            }
        });
        add(firstBar, c);
        c.gridy++;

        secondBar = new JTextArea(Second);
        secondBar.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        secondBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        secondBar.setLineWrap(true);
        secondBar.setWrapStyleWord(true);
        secondBar.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        secondBar.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (secondBar.getText().equals(Second)) {
                    secondBar.setText("");
                    secondBar.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (secondBar.getText().isEmpty()) {
                    secondBar.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
                    secondBar.setText(Second);
                }
            }
        });
        add(secondBar, c);
        c.gridy++;

        JPanel refreshPanel = new JPanel();
        refreshPanel.setLayout(new BorderLayout());
        JButton refreshButton = new JButton("Generate Transcript");
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener((event) ->
        {
            String startPoint = firstBar.getText();
            String endPoint = secondBar.getText();
            if (startPoint.equals(First) || endPoint.equals(Second)) {
                Output = Error;
                OutputField.setText(Output);
                return;
            }
            if (!scrubChat(startPoint.trim(), endPoint.trim())) {
                Output = Error;
                OutputField.setText(Output);
                return;
            }
        });
        refreshPanel.add(refreshButton, BorderLayout.CENTER);
        add(refreshPanel, c);
        c.gridy++;

        OutputField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        OutputField.setLineWrap(true);
        OutputField.setWrapStyleWord(true);
        OutputField.setMinimumSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        add(OutputField, c);
        c.gridy++;

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());
        imageButton.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        imageButton.setFocusPainted(false);
        imageButton.addActionListener((event) ->
        {
            if (Ready) {
                try {
                    makeImage(Transcript);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        imagePanel.add(imageButton, BorderLayout.CENTER);
        add(imagePanel, c);
        c.gridy++;


    }

    static String format(Date date) {
        synchronized (TIME_FORMAT) {
            return TIME_FORMAT.format(date);
        }
    }

    private Boolean scrubChat(String start, String end) {
        Ready = false;
        if (client.getWidget(162, 58) != null) {
            Widget[] Testing = client.getWidget(162, 58).getDynamicChildren();
            if (Testing.length == 0) {
                return (false);
            }
            String check = "";
            String temp = "";
            String tempSplit = "";
            String finalSplit = "";
            String out = "";
            Boolean first = false;
            Boolean last = false;
            Transcript = "";
            if (start.equals("^all") && end.equals("all$")) {
                first = true;
                last = true;
            }
            for (int x = Testing.length - 1; x >= 0; x--) {
                if (!Testing[x].getText().isEmpty() && !Testing[x + 1].getText().isEmpty()
                        && (Testing[x].getRelativeY() == Testing[x + 1].getRelativeY())) {
                    check = Testing[x].getText() + " " + Testing[x + 1].getText();

                    if (check.split("<col=.{6}>").length > 0) {
                        temp = "";
                        tempSplit = "";
                        finalSplit = "";
                        for (String hold : check.split("<col=.{6}>")) {
                            temp += hold;
                        }
                        for (String hold : temp.split("</col>")) {
                            tempSplit += hold;
                        }
                        for (String hold : tempSplit.split("<img=\\d{1,3}>")) {
                            finalSplit += hold.trim();
                        }
                    }
                    if (finalSplit.trim().toLowerCase().endsWith(start.toLowerCase())) {
                        first = true;
                    }
                    if (first && finalSplit.trim().toLowerCase().endsWith(end.toLowerCase())) {
                        out += finalSplit;
                        Transcript += Testing[x].getText() + " " + Testing[x + 1].getText();
                        last = true;
                        break;
                    }
                    if (!finalSplit.isEmpty() && first) {
                        Transcript += Testing[x].getText() + " " + Testing[x + 1].getText() + "\n";
                        out += finalSplit + "\n";
                    }
                }
            }
            if (!out.isEmpty() && last) {
                if (start.equals("^all") && end.equals("all$"))
                    out = out.substring(0, out.lastIndexOf("\n"));
                Output = out;
                OutputField.setText(Output);
                Ready = true;
                imageButton.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
                return (true);
            }
        }
        imageButton.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        return (false);
    }

    private void makeImage(String chat) throws IOException {

        String newTranscript = Transcript.replaceAll("<col=", "<font color=#").replaceAll("</col>", "</font color>").replaceAll("\n", "<br>").replaceAll("<img=\\d*>", "");
        String newerTranscript = "";
        String[] newSplit = newTranscript.split("<br>");
        for (int x = 0; x < newSplit.length; x++) {
            if (newSplit[x].split("<font color=#.{6}>").length != newSplit[x].split("</font color>").length) {
                for (int y = 0; y < newSplit[x].split("<font color=#.{6}>").length - newSplit[x].split("</font color>").length; y++) {
                    newSplit[x] += "</font color>";
                }
            }
            if (x != newSplit.length - 1)
                newSplit[x] += "<br>";
            newerTranscript += newSplit[x];
        }
        /* At some point I'll revisit this to get images working but til then not my problem

        if(newTranscript.contains("<img=")) {
            String img="";
            String img2="";
            img=newTranscript.substring(newTranscript.indexOf("<img=")+5);
            if(newTranscript.indexOf("<img=")!=newTranscript.lastIndexOf("<img="))
            {
                img2=img.substring(newTranscript.indexOf("<img=")+5);
                img=img.substring(0,img.indexOf(">"));
                img2=img2.substring(0,img.indexOf(">"));
                newTranscript=newTranscript.replaceFirst("<img=\\d*>","<img src=\"../resources/"+img+".png\">");
                newTranscript=newTranscript.replaceFirst("<img=\\d*>","<img src=\"../resources/"+img2+".png\">");
            }else{
                img=img.substring(0,img.indexOf(">"));
                newTranscript=newTranscript.replaceFirst("<img=\\d*>","<img="WHY DOESNT THIS WORK">");
            }
            System.out.println(newTranscript);
        }
         */
        JLabel label = new JLabel("<html>" + newerTranscript + "</html>");
        label.setBackground(new Color(208, 188, 157));
        label.setForeground(Color.BLACK);
        label.setOpaque(true);
        int width = label.getPreferredSize().width;
        int height = label.getPreferredSize().height;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g2d = bufferedImage.createGraphics();
        SwingUtilities.paintComponent(g2d, label, new CellRendererPane(), 0, 0, width, height);
        g2d.dispose();
        File parentFolder = new File(SCREENSHOT_DIR, "Transcripts");
        parentFolder.mkdirs();
        File file = new File(parentFolder, client.getLocalPlayer().getName() + format(new Date()) + ".png");
        try {
            ImageIO.write(bufferedImage, "png", file);
            OutputField.setText("Transcript saved to Screenshots folder.");
            firstBar.setText(First);
            firstBar.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
            secondBar.setText(Second);
            secondBar.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}