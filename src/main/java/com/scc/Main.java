package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // The app only starts if the database is ready
        if (!DatabaseInitializer.initialize()) {
            System.out.println("Cannot start: database setup failed.");
            return;
        }

        Authentication authentication = new Authentication(new Scanner(System.in));
        authentication.run();
    }
}