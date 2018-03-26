package edu.mills.cs250.dosemakespoison;

import android.database.sqlite.SQLiteDatabase;

/**
 * Provides a view of a chemical in the user's pantry.
 */

public class Chem {

    private String spectrum, name, description;
    private int ld50Num;
    private SQLiteDatabase db;

    /**
     * Constructs a chemical.
     *
     * @param spectrum    the chemical shown on the toxicity spectrum
     * @param name        the chemical name
     * @param ld50Num     the LD50 number of the chemical
     * @param description the chemical description
     */
    public Chem(String spectrum, String name, int ld50Num, String description) {
        this.spectrum = spectrum;
        this.name = name;
        this.ld50Num = ld50Num;
        this.description = description;
    }

    /**
     * Gets the spectrum.
     *
     * @return the spectrum
     */
    public String getSpectrum() {
        return spectrum;
    }

    /**
     * Sets the spectrum.
     *
     * @param spectrum the chemical spectrum
     */
    public void setSpectrum(String spectrum) {
        this.spectrum = spectrum;
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
     * Sets the name, otherwise known as title.
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
    public int getLd50Num() {
        return ld50Num;
    }

    /**
     * Sets the LD50 number.
     *
     * @param ld50Num the LD50 number of the chemical
     */
    public void setLd50Num(int ld50Num) {
        this.ld50Num = ld50Num;
    }

    /**
     * Gets the description.
     *
     * @return the chemical description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the chemical description
     */
    public void setDescription(String description) {
        this.description = description;
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
