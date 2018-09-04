/*
 * Implementation of Dose Makes the Poison application. Created for Mills
 * CS250: Master's Thesis, Spring 2018.
 *
 * @author Kate Manning
 */
package edu.mills.cs250.toxsense;

import android.database.sqlite.SQLiteDatabase;

/**
 * Provides a view of a chemical in the user's pantry.
 */

public class Chem {

    private String name, chemId, comparisonChem;
    private int ld50Val, comparisonViewId;

    /**
     * Constructs a chemical.
     *
     * @param name              the chemical name
     * @param chemId            the ChemID database number of the chemical
     * @param ld50Val           the LD50 value of the chemical
     * @param comparisonChem    the spectrum comparison chemical with closest LD50 value
     * @param comparisonViewId  the number of block where the chemical falls on the toxicity spectrum
     */
    Chem (String name, String chemId, int ld50Val, String comparisonChem, int comparisonViewId) {
        this.name = name;
        this.chemId = chemId;
        this.ld50Val = ld50Val;
        this.comparisonChem = comparisonChem;
        this.comparisonViewId = comparisonViewId;
    }

    /**
     * Constructs a chemical.
     *
     * @param name          the chemical name
     * @param chemId     the ChemID database number of the chemical
     */
    Chem (String name, String chemId) {
        this.name = name;
        this.chemId = chemId;
        this.ld50Val = -1;
        this.comparisonChem = "";
        this.comparisonViewId = -1;
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
     * Gets the ChemID database number.
     *
     * @return the ChemID number of the chemical
     */
    public String getChemId() {
        return chemId;
    }

    /**
     * Sets the ChemID number.
     *
     * @param chemId the ChemID number of the chemical
     */
    public void setChemId(String chemId) {
        this.chemId = chemId;
    }

    /**
     * Gets the LD50 value.
     *
     * @return the LD50 value of the chemical
     */
    public int getLd50Val() {
        return ld50Val;
    }

    /**
     * Sets the LD50 value.
     *
     * @param ld50Val the LD50 value of the chemical
     */
    public void setLd50Val(int ld50Val) {
        this.ld50Val = ld50Val;
    }

    /**
     * Gets the comparison chemical.
     *
     * @return the comparison chemical on the spectrum closest in LD50 value
     */
    public String getComparisonChem() {
        return comparisonChem;
    }

    /**
     * Sets the comparison chemical.
     *
     * @param comparisonChem the comparison chemical closest on the toxicity spectrum
     */
    public void setComparisonChem(String comparisonChem) {
        this.comparisonChem = comparisonChem;
    }

    /**
     * Gets the spectrum position number.
     *
     * @return the spectrum block number
     */
    public int getComparisonViewId() {
        return comparisonViewId;
    }

    /**
     * Sets the spectrum position number.
     *
     * @param comparisonViewId the position number on the toxicity spectrum of the chemical
     */
    public void setComparisonViewId(int comparisonViewId) {
        this.comparisonViewId = comparisonViewId;
    }

}
