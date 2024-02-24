package org.example;

public class ShowArgs {
     public static void main (String[] args) {
          System.out.println("No. of input parameters = "+args.length);
          if (args.length > 0) {
                System.out.println("The input parameters are listed below:");
                for (int i=0; i<=args.length-1; i++) {
                      System.out.println("Parameter # "+i+" => "+args[i]);
                    }
              }
        }
      }