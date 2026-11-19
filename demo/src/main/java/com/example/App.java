package com.example;

public class App 
{
    public static void main( String[] args )
    {
        try {
            new Game().Run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
