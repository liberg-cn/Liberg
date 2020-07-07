package cn.liberg.support;

import java.util.Random;

public class RandomString {
    private static char[] NUMBERS = "0123456789".toCharArray();
    private static char[] LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static char[] CHARS = new char[NUMBERS.length+ LETTERS.length];
    private int minLength = 0;
    private int maxLength = 1000;
    private Random random = new Random();

    public RandomString(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    static {
        for (int i = 0; i < NUMBERS.length; i++) {
            CHARS[i] = NUMBERS[i];
        }
        for (int i = 0; i < LETTERS.length; i++) {
            CHARS[NUMBERS.length+i] = LETTERS[i];
        }
    }

    public String next() {
        int length = minLength + random.nextInt(maxLength-minLength+1);
        char[] array = new char[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = CHARS[random.nextInt(CHARS.length)];
        }
        return new String(array);
    }

    public String next(int minLength, int maxLength) {
        int length = minLength + random.nextInt(maxLength-minLength+1);
        char[] array = new char[length];
        for (int i = 0; i < array.length; i++) {
            array[i] = CHARS[random.nextInt(CHARS.length)];
        }
        return new String(array);
    }
}
