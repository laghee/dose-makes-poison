/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.dosemakespoison;

import android.database.sqlite.SQLiteDatabase;

/**
 * Provides a view of a chemical in the user's pantry.
 */

public class Chem {

    private String name, compareChem;
    private int ld50Val, spectrumNum;
    private SQLiteDatabase db;

    /**
     * Constructs a chemical.
     *
     * @param name        the chemical name
     * @param ld50Val     the LD50 number of the chemical
     * @param compareChem the spectrum comparison chemical with closest LD50 value
     * @param spectrumNum the number of block where the chemical falls on the toxicity spectrum
     */
    Chem (String name, int ld50Val, String compareChem, int spectrumNum) {
        this.name = name;
        this.ld50Val = ld50Val;
        this.compareChem = compareChem;
        this.spectrumNum = spectrumNum;
    }

    /**
     * Gets the name.
     *
     * @return the name of the chemical
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name of the chemical
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the LD50 number.
     *
     * @return the LD50 number of the chemical
     */
    public int getLd50Val() {
        return ld50Val;
    }

    /**
     * Sets the LD50 number.
     *
     * @param ld50Val the LD50 number of the chemical
     */
    public void setLd50Val(int ld50Val) {
        this.ld50Val = ld50Val;
    }

    /**
     * Gets the comparison chemical.
     *
     * @return the comparison chemical on the spectrum closest in LD50 value
     */
    public String getCompareChem() {
        return compareChem;
    }

    /**
     * Sets the comparison chemical.
     *
     * @param compareChem the the comparison chemical closest on the toxicity spectrum
     */
    public void setCompareChem(String compareChem) {
        this.compareChem = compareChem;
    }

    /**
     * Gets the spectrum position number.
     *
     * @return the spectrum block number
     */
    public int getSpectrumNum() {
        return spectrumNum;
    }

    /**
     * Sets the spectrum position number.
     *
     * @param spectrumNum the position number on the toxicity spectrum of the chemical
     */
    public void setSpectrumNum(int spectrumNum) {
        this.spectrumNum = spectrumNum;
    }

    /**
     * Gets the database.
     *
     * @return the SQLite database
     */
    public SQLiteDatabase getDb() {
        return db;
    }

    /**
     * Sets the database.
     *
     * @param db the SQLite database
     */
    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

}
