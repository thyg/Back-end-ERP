package com.rtcomops.treasury.util;

import java.math.BigInteger;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for validating International Bank Account Numbers (IBAN).
 *
 * <p>Provides comprehensive IBAN validation including:</p>
 * <ul>
 *   <li>Basic format validation (2 letters + 2 digits + 4-30 alphanumeric characters)</li>
 *   <li>Country-specific length validation</li>
 *   <li>MOD-97 checksum validation (ISO 7064)</li>
 * </ul>
 *
 * @author RT-ComOps Team
 * @version 1.0.0
 * @since 2024-12-24
 */
public final class IbanValidator {

    /**
     * Basic IBAN pattern: 2 letters (country code) + 2 digits (check digits) + 11-30 alphanumeric characters.
     */
    private static final Pattern IBAN_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}$");

    /**
     * IBAN lengths by country code (ISO 3166-1 alpha-2).
     * This map contains the expected IBAN length for each supported country.
     */
    private static final Map<String, Integer> IBAN_LENGTHS = Map.ofEntries(
        // Europe
        Map.entry("FR", 27),  // France
        Map.entry("DE", 22),  // Germany
        Map.entry("ES", 24),  // Spain
        Map.entry("IT", 27),  // Italy
        Map.entry("GB", 22),  // United Kingdom
        Map.entry("BE", 16),  // Belgium
        Map.entry("CH", 21),  // Switzerland
        Map.entry("NL", 18),  // Netherlands
        Map.entry("AT", 20),  // Austria
        Map.entry("PT", 25),  // Portugal
        Map.entry("LU", 20),  // Luxembourg
        Map.entry("IE", 22),  // Ireland
        Map.entry("MC", 27),  // Monaco
        Map.entry("AD", 24),  // Andorra

        // Africa - CEMAC Zone (XAF)
        Map.entry("CM", 27),  // Cameroon
        Map.entry("CF", 27),  // Central African Republic
        Map.entry("TD", 27),  // Chad
        Map.entry("CG", 27),  // Republic of the Congo
        Map.entry("GQ", 27),  // Equatorial Guinea
        Map.entry("GA", 27),  // Gabon

        // Africa - UEMOA Zone (XOF)
        Map.entry("BJ", 28),  // Benin
        Map.entry("BF", 28),  // Burkina Faso
        Map.entry("CI", 28),  // Ivory Coast
        Map.entry("GW", 25),  // Guinea-Bissau
        Map.entry("ML", 28),  // Mali
        Map.entry("NE", 28),  // Niger
        Map.entry("SN", 28),  // Senegal
        Map.entry("TG", 28),  // Togo

        // Other Africa
        Map.entry("MU", 30),  // Mauritius
        Map.entry("MG", 27),  // Madagascar
        Map.entry("TN", 24),  // Tunisia
        Map.entry("DZ", 26),  // Algeria
        Map.entry("MA", 28),  // Morocco

        // Middle East
        Map.entry("SA", 24),  // Saudi Arabia
        Map.entry("AE", 23),  // United Arab Emirates
        Map.entry("QA", 29),  // Qatar
        Map.entry("KW", 30),  // Kuwait
        Map.entry("BH", 22),  // Bahrain

        // Other
        Map.entry("TR", 26),  // Turkey
        Map.entry("IL", 23),  // Israel
        Map.entry("BR", 29)   // Brazil
    );

    /**
     * MOD-97 value used for IBAN checksum validation (ISO 7064).
     */
    private static final BigInteger MOD_97 = BigInteger.valueOf(97);

    /**
     * Private constructor to prevent instantiation.
     */
    private IbanValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates an IBAN string.
     *
     * <p>This method performs the following validations:</p>
     * <ol>
     *   <li>Null/empty check (returns true for empty - IBAN is optional)</li>
     *   <li>Basic format validation using regex</li>
     *   <li>Country-specific length validation (if country is known)</li>
     *   <li>MOD-97 checksum validation</li>
     * </ol>
     *
     * @param iban the IBAN to validate (can be null, empty, or contain spaces)
     * @return true if the IBAN is valid or empty, false otherwise
     */
    public static boolean isValid(String iban) {
        // IBAN is optional - null or blank is valid
        if (iban == null || iban.isBlank()) {
            return true;
        }

        // Clean the IBAN: remove spaces and convert to uppercase
        String cleanIban = iban.replaceAll("\\s", "").toUpperCase();

        // Check basic format
        if (!IBAN_PATTERN.matcher(cleanIban).matches()) {
            return false;
        }

        // Check country-specific length
        String countryCode = cleanIban.substring(0, 2);
        Integer expectedLength = IBAN_LENGTHS.get(countryCode);
        if (expectedLength != null && cleanIban.length() != expectedLength) {
            return false;
        }

        // Validate MOD-97 checksum
        return validateChecksum(cleanIban);
    }

    /**
     * Validates the MOD-97 checksum of an IBAN.
     *
     * <p>Algorithm:</p>
     * <ol>
     *   <li>Move the first 4 characters to the end</li>
     *   <li>Replace each letter with two digits (A=10, B=11, ..., Z=35)</li>
     *   <li>The resulting number modulo 97 must equal 1</li>
     * </ol>
     *
     * @param iban the cleaned IBAN (uppercase, no spaces)
     * @return true if checksum is valid
     */
    private static boolean validateChecksum(String iban) {
        // Rearrange: move first 4 chars to end
        String rearranged = iban.substring(4) + iban.substring(0, 4);

        // Convert letters to numbers
        StringBuilder numericIban = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isDigit(c)) {
                numericIban.append(c);
            } else {
                // A=10, B=11, ..., Z=35
                numericIban.append(Character.getNumericValue(c));
            }
        }

        // Calculate modulo 97
        BigInteger ibanNumber = new BigInteger(numericIban.toString());
        return ibanNumber.mod(MOD_97).intValue() == 1;
    }

    /**
     * Formats an IBAN with spaces every 4 characters for display.
     *
     * @param iban the IBAN to format
     * @return the formatted IBAN, or the original if null/empty
     */
    public static String format(String iban) {
        if (iban == null || iban.isBlank()) {
            return iban;
        }

        String cleanIban = iban.replaceAll("\\s", "").toUpperCase();
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < cleanIban.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(' ');
            }
            formatted.append(cleanIban.charAt(i));
        }

        return formatted.toString();
    }

    /**
     * Gets the expected IBAN length for a country.
     *
     * @param countryCode the ISO 3166-1 alpha-2 country code
     * @return the expected length, or null if unknown
     */
    public static Integer getExpectedLength(String countryCode) {
        if (countryCode == null || countryCode.length() != 2) {
            return null;
        }
        return IBAN_LENGTHS.get(countryCode.toUpperCase());
    }

    /**
     * Extracts the country code from an IBAN.
     *
     * @param iban the IBAN
     * @return the 2-letter country code, or null if invalid
     */
    public static String getCountryCode(String iban) {
        if (iban == null || iban.length() < 2) {
            return null;
        }
        return iban.replaceAll("\\s", "").substring(0, 2).toUpperCase();
    }
}
