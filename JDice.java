package jdice; // ✅ Sửa lỗi: cần package

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.util.logging.Logger; // ✅ Thêm Logger chuẩn thay vì System.out
import java.util.logging.Level;

/*
JDice: Java Dice Rolling Program
Copyright (C) 2006 Andrew D. Hilton (adhilton@cis.upenn.edu)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
*/

public class JDice {
    private static final Logger LOGGER = Logger.getLogger(JDice.class.getName()); // ✅ Thêm logger

    static final String CLEAR = "Clear"; // ✅ Sửa lỗi gán sai Clear
    static final String ROLL = "Roll Selection";

    static void showError(String s) {
        JOptionPane.showMessageDialog(null, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static class JDiceListener implements ActionListener {
        Vector<String> listItems;
        JList<String> resultList;
        JComboBox<String> inputBox;
        long lastEvent;

        public JDiceListener(JList<String> resultList, JComboBox<String> inputBox) { // ✅ Sửa tên constructor
            this.listItems = new Vector<>();
            this.resultList = resultList;
            this.inputBox = inputBox;
            lastEvent = 0;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getWhen() == lastEvent) {
                return;
            }
            lastEvent = e.getWhen();

            if (e.getSource() instanceof JComboBox || ROLL.equals(e.getActionCommand())) { // ✅ Gọi đúng static
                String s = inputBox.getSelectedItem().toString();
                String[] arr = s.split("=");
                StringBuilder nameBuilder = new StringBuilder(); // ✅ Dùng StringBuilder
                for (int i = 0; i < arr.length - 2; i++) {
                    nameBuilder.append(arr[i]).append("=");
                }
                String name = "";
                if (arr.length >= 2) {
                    name = nameBuilder.append(arr[arr.length - 2]).toString();
                }
                doRoll(name, arr[arr.length - 1]);
            } else if (CLEAR.equals(e.getActionCommand())) { // ✅ Gọi đúng static
                doClear();
            }
        }

        private void doClear() {
            resultList.clearSelection();
            listItems.clear();
            resultList.setListData(listItems);
        }

        private void doRoll(String name, String diceString) {
            String prepend = "";
            int start = 0;
            int i;
            Vector<DieRoll> v = DiceParser.parseRoll(diceString);
            if (v == null) {
                JDice.showError("Invalid dice string: " + diceString);
                return;
            }
            if (name != null) {
                listItems.add(0, name);
                start = 1;
                prepend = "  ";
            }
            int[] selectionIndices = new int[start + v.size()];
            for (i = 0; i < v.size(); i++) {
                DieRoll dr = v.get(i);
                RollResult rr = dr.makeRoll();
                String toAdd = prepend + dr + "  =>  " + rr;
                listItems.add(i + start, toAdd);
            }
            for (i = 0; i < selectionIndices.length; i++) {
                selectionIndices[i] = i;
            }
            resultList.setListData(listItems);
            resultList.setSelectedIndices(selectionIndices);
        }
    }

    public static void main(String[] args) {
        Vector<String> v = new Vector<>();
        if (args.length >= 1) {
            try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) { // ✅ Auto-close resource
                String s;
                while ((s = br.readLine()) != null) {
                    v.add(s);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "Could not read input file: " + args[0], ioe); // ✅ Dùng LOGGER
            }
        }

        JFrame jf = new JFrame("Dice Roller");
        Container c = jf.getContentPane();
        c.setLayout(new BorderLayout());

        JList<String> jl = new JList<>();
        c.add(new JScrollPane(jl), BorderLayout.CENTER); // ✅ Cho JList vào JScrollPane

        JComboBox<String> jcb = new JComboBox<>(v);
        jcb.setEditable(true);
        c.add(jcb, BorderLayout.NORTH);

        JDiceListener jdl = new JDiceListener(jl, jcb);
        jcb.addActionListener(jdl);

        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));

        String[] buttons = {
            ROLL, "d4", "d6", "d8", "d10", "d12", "d20", "d100", CLEAR
        };

        for (String button : buttons) {
            JButton newButton = new JButton(button);
            rightSide.add(newButton);
            newButton.addActionListener(jdl);
        }

        c.add(rightSide, BorderLayout.EAST);

        jf.setSize(450, 500);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
    }
}
