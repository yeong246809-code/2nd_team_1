package org.example.k_market.service.admin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class VersionCodeComparator implements Comparator<String> {

    static final VersionCodeComparator INSTANCE = new VersionCodeComparator();

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    private VersionCodeComparator() {
    }

    @Override
    public int compare(String left, String right) {
        List<BigInteger> leftNumbers = numericParts(left);
        List<BigInteger> rightNumbers = numericParts(right);
        int maxLength = Math.max(leftNumbers.size(), rightNumbers.size());

        for (int index = 0; index < maxLength; index++) {
            BigInteger leftPart = index < leftNumbers.size() ? leftNumbers.get(index) : BigInteger.ZERO;
            BigInteger rightPart = index < rightNumbers.size() ? rightNumbers.get(index) : BigInteger.ZERO;
            int comparison = leftPart.compareTo(rightPart);
            if (comparison != 0) {
                return comparison;
            }
        }

        boolean leftSnapshot = isSnapshot(left);
        boolean rightSnapshot = isSnapshot(right);
        if (leftSnapshot != rightSnapshot) {
            return leftSnapshot ? -1 : 1;
        }

        return safe(left).compareToIgnoreCase(safe(right));
    }

    private List<BigInteger> numericParts(String versionCode) {
        List<BigInteger> parts = new ArrayList<>();
        Matcher matcher = NUMBER_PATTERN.matcher(safe(versionCode));
        while (matcher.find()) {
            parts.add(new BigInteger(matcher.group()));
        }
        return parts;
    }

    private boolean isSnapshot(String versionCode) {
        return safe(versionCode).toUpperCase().contains("SNAPSHOT");
    }

    private String safe(String versionCode) {
        return versionCode == null ? "" : versionCode.trim();
    }
}
