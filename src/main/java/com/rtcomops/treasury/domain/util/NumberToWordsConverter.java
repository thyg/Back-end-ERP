package com.rtcomops.treasury.domain.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for converting numbers to French words.
 *
 * <p>Converts monetary amounts to their French text representation,
 * supporting various currencies including EUR, XAF (FCFA), USD, etc.</p>
 */
public final class NumberToWordsConverter {

    private static final String[] UNITS = {
        "", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
        "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize", "dix-sept", "dix-huit", "dix-neuf"
    };

    private static final String[] TENS = {
        "", "", "vingt", "trente", "quarante", "cinquante", "soixante", "soixante", "quatre-vingt", "quatre-vingt"
    };

    private NumberToWordsConverter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String convert(BigDecimal amount, String currency) {
        if (amount == null) {
            return "";
        }

        amount = amount.setScale(2, RoundingMode.HALF_UP);

        long wholePart = amount.longValue();
        int decimalPart = amount.remainder(BigDecimal.ONE)
            .multiply(BigDecimal.valueOf(100))
            .intValue();

        StringBuilder result = new StringBuilder();

        if (wholePart == 0 && decimalPart == 0) {
            result.append("zéro");
        } else if (wholePart > 0) {
            result.append(convertNumber(wholePart));
        }

        String currencyName = getCurrencyName(currency, wholePart);
        if (wholePart > 0) {
            result.append(" ").append(currencyName);
        }

        if (decimalPart > 0) {
            if (wholePart > 0) {
                result.append(" et ");
            }
            result.append(convertNumber(decimalPart));
            result.append(" ").append(getCentimesName(currency, decimalPart));
        }

        String text = result.toString().trim();
        if (!text.isEmpty()) {
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        return text;
    }

    public static String convertNumber(long number) {
        if (number == 0) {
            return "zéro";
        }
        if (number < 0) {
            return "moins " + convertNumber(-number);
        }

        StringBuilder result = new StringBuilder();

        if (number >= 1_000_000_000) {
            long milliards = number / 1_000_000_000;
            if (milliards == 1) {
                result.append("un milliard");
            } else {
                result.append(convertNumber(milliards)).append(" milliards");
            }
            number %= 1_000_000_000;
            if (number > 0) {
                result.append(" ");
            }
        }

        if (number >= 1_000_000) {
            long millions = number / 1_000_000;
            if (millions == 1) {
                result.append("un million");
            } else {
                result.append(convertNumber(millions)).append(" millions");
            }
            number %= 1_000_000;
            if (number > 0) {
                result.append(" ");
            }
        }

        if (number >= 1000) {
            long milliers = number / 1000;
            if (milliers == 1) {
                result.append("mille");
            } else {
                result.append(convertHundreds((int) milliers)).append(" mille");
            }
            number %= 1000;
            if (number > 0) {
                result.append(" ");
            }
        }

        if (number > 0) {
            result.append(convertHundreds((int) number));
        }

        return result.toString();
    }

    private static String convertHundreds(int number) {
        if (number == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        if (number >= 100) {
            int hundreds = number / 100;
            if (hundreds == 1) {
                result.append("cent");
            } else {
                result.append(UNITS[hundreds]).append(" cent");
            }
            number %= 100;

            if (number == 0 && hundreds > 1) {
                result.append("s");
            } else if (number > 0) {
                result.append(" ");
            }
        }

        if (number > 0) {
            result.append(convertTens(number));
        }

        return result.toString();
    }

    private static String convertTens(int number) {
        if (number == 0) {
            return "";
        }

        if (number < 20) {
            return UNITS[number];
        }

        int tens = number / 10;
        int units = number % 10;

        StringBuilder result = new StringBuilder();

        switch (tens) {
            case 7:
                if (units == 1) {
                    result.append("soixante et onze");
                } else {
                    result.append("soixante-").append(UNITS[10 + units]);
                }
                break;

            case 8:
                if (units == 0) {
                    result.append("quatre-vingts");
                } else {
                    result.append("quatre-vingt-").append(UNITS[units]);
                }
                break;

            case 9:
                result.append("quatre-vingt-").append(UNITS[10 + units]);
                break;

            default:
                result.append(TENS[tens]);
                if (units == 1 && tens != 8) {
                    result.append(" et un");
                } else if (units > 0) {
                    result.append("-").append(UNITS[units]);
                }
                break;
        }

        return result.toString();
    }

    private static String getCurrencyName(String currency, long amount) {
        if (currency == null) {
            return "";
        }

        switch (currency.toUpperCase()) {
            case "EUR":
                return amount > 1 ? "euros" : "euro";
            case "XAF":
            case "FCFA":
            case "XOF":
                return "francs CFA";
            case "USD":
                return amount > 1 ? "dollars" : "dollar";
            case "GBP":
                return amount > 1 ? "livres sterling" : "livre sterling";
            case "CHF":
                return amount > 1 ? "francs suisses" : "franc suisse";
            case "CAD":
                return amount > 1 ? "dollars canadiens" : "dollar canadien";
            case "JPY":
                return "yens";
            default:
                return currency;
        }
    }

    private static String getCentimesName(String currency, int amount) {
        if (currency == null) {
            return "centimes";
        }

        switch (currency.toUpperCase()) {
            case "EUR":
            case "CHF":
                return amount > 1 ? "centimes" : "centime";
            case "XAF":
            case "FCFA":
            case "XOF":
                return "centimes";
            case "USD":
            case "CAD":
                return amount > 1 ? "cents" : "cent";
            case "GBP":
                return amount > 1 ? "pence" : "penny";
            default:
                return "centimes";
        }
    }
}
