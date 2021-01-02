/*
    Foilen Infra Plugin
    https://github.com/foilen/foilen-infra-plugin
    Copyright (c) 2017-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.plugin.v1.core.visual.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;

import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Strings;

public class CommonValidation {

    private static Pattern alphaNumLowerValidationRegex = Pattern.compile("[a-z0-9\\.\\_\\-]+");
    private static Pattern alphaNumLowerAndUpperValidationRegex = Pattern.compile("[a-zA-Z0-9\\.\\_\\-]+");
    private static Pattern pathValidationRegex = Pattern.compile("[a-zA-Z0-9\\.\\_\\-\\/\\ ]+");

    private static int count(String hayStash, char needle) {
        int count = 0;
        for (int i = 0; i < hayStash.length(); ++i) {
            char c = hayStash.charAt(i);
            if (c == needle) {
                ++count;
            }
        }
        return count;
    }

    private static boolean isCronTime(String part, int min, int max) {
        String[] slashParts = part.split("/");
        switch (slashParts.length) {
        case 1:
            // *
            if ("*".equals(part)) {
                return true;
            }

            // Single value
            try {
                Integer asInt = Integer.valueOf(part);
                return asInt >= min && asInt <= max;
            } catch (Exception e) {
            }

            // Multiple values
            String[] comaParts = part.split(",");
            if (count(part, ',') + 1 == comaParts.length) {
                boolean allGood = true;
                for (String comaPart : comaParts) {
                    allGood &= isCronTime(comaPart, min, max);
                }
                return allGood;
            }
            return false;

        case 2:
            return isCronTime(slashParts[0], min, max) && isCronTime(slashParts[1], min, max);

        default:
            return false;
        }
    }

    @Deprecated
    public static List<Tuple2<String, String>> validateAlphaNum(Map<String, String> formValues) {
        return validateAlphaNumLower(formValues);
    }

    @Deprecated
    public static List<Tuple2<String, String>> validateAlphaNum(Map<String, String> formValues, String... fieldNames) {
        return validateAlphaNumLower(formValues, fieldNames);
    }

    /**
     *
     * @param fieldName
     *            the name of the field
     * @param fieldValue
     *            the value of the field
     * @return the errors
     * @deprecated use {@link #validateAlphaNumLower(String, String)}
     */
    @Deprecated
    public static List<Tuple2<String, String>> validateAlphaNum(String fieldName, String fieldValue) {
        return validateAlphaNumLower(fieldName, fieldValue);
    }

    public static List<Tuple2<String, String>> validateAlphaNumLower(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateAlphaNum(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateAlphaNumLower(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateAlphaNumLower(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateAlphaNumLower(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            if (!alphaNumLowerValidationRegex.matcher(fieldValue).matches()) {
                errors.add(new Tuple2<>(fieldName, "error.notAlphaNumLower"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateAlphaNumLowerAndUpper(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateAlphaNum(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateAlphaNumLowerAndUpper(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateAlphaNumLowerAndUpper(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateAlphaNumLowerAndUpper(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            if (!alphaNumLowerAndUpperValidationRegex.matcher(fieldValue).matches()) {
                errors.add(new Tuple2<>(fieldName, "error.notAlphaNumLowerAndUpper"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateCronTime(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateCronTime(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateCronTime(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateCronTime(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateCronTime(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            String[] parts = fieldValue.split(" ");
            boolean valid = parts.length == 5;

            if (valid) {
                // Minutes: 0-59
                int i = 0;
                valid &= isCronTime(parts[i++], 0, 59);
                // Hour: 0-23
                valid &= isCronTime(parts[i++], 0, 23);
                // Day: 1-31
                valid &= isCronTime(parts[i++], 1, 31);
                // Month: 1-12 or JAN-DEC
                valid &= isCronTime(parts[i++], 1, 12);
                // Day of week: 0-6 OR SUN-SAT
                valid &= isCronTime(parts[i++], 0, 6);
            }

            if (!valid) {
                errors.add(new Tuple2<>(fieldName, "error.notCronTime"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateDayFormat(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            try {
                DateTools.parseDateOnly(fieldValue);
            } catch (Exception e) {
                errors.add(new Tuple2<>(fieldName, "error.dayFormat"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateDomainName(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateDomainName(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateDomainName(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateDomainName(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateDomainName(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (fieldValue != null) {
            if (!DomainValidator.getInstance().isValid(fieldValue.replaceAll("_", "a"))) {
                errors.add(new Tuple2<>(fieldName, "error.notDomain"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateEmail(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateEmail(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateEmail(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateEmail(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateEmail(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            if (!EmailValidator.getInstance().isValid(fieldValue)) {
                errors.add(new Tuple2<>(fieldName, "error.notEmail"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateIdem(Map<String, String> formValues, String fieldNames1, String fieldNames2) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        Object first = formValues.get(fieldNames1);
        Object second = formValues.get(fieldNames2);
        if ((first == null && second != null) || //
                (first != null && !first.equals(second)) //
        ) {
            errors.add(new Tuple2<>(fieldNames2, "error.notIdem"));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateInEnum(Map<String, String> formValues, Enum<?>[] values, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String value = formValues.get(fieldName);
            boolean isValid = value != null //
                    && Arrays.asList(values).stream().anyMatch(it -> value.equals(it.name()));
            if (!isValid) {
                errors.add(new Tuple2<>(fieldName, "error.notValidChoice"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateInList(Map<String, String> formValues, List<String> validValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateInList(fieldName, fieldValue, validValues));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateInList(String fieldName, String fieldValue, List<String> validValues) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        boolean isValid = fieldValue != null //
                && validValues.contains(fieldValue);
        if (!isValid) {
            errors.add(new Tuple2<>(fieldName, "error.notValidChoice"));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateIpAddress(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateIpAddress(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateIpAddress(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            boolean isIp = true;

            String[] parts = fieldValue.split("\\.");
            if (parts.length == 4) {
                for (int i = 0; i < 4 && isIp; ++i) {
                    try {
                        int part = Integer.parseInt(parts[i]);
                        if (part < 0 || part > 255) {
                            isIp = false;
                        }
                    } catch (Exception e) {
                        isIp = false;
                    }
                }
            } else {
                isIp = false;
            }

            if (!isIp) {
                errors.add(new Tuple2<>(fieldName, "error.notIp"));
            }

        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateNoMultiline(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateNoMultiline(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateNoMultiline(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateNoMultiline(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateNoMultiline(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            if (fieldValue.contains("\n") || fieldValue.contains("\r")) {
                errors.add(new Tuple2<>(fieldName, "error.noMultiline"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateNotNullOrEmpty(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateNotNullOrEmpty(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateNotNullOrEmpty(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            if (Strings.isNullOrEmpty(fieldValue)) {
                errors.add(new Tuple2<>(fieldName, "error.required"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateNotNullOrEmpty(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (Strings.isNullOrEmpty(fieldValue)) {
            errors.add(new Tuple2<>(fieldName, "error.required"));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateOneAndOnlyOneNotNull(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateOneAndOnlyOneNotNull(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateOneAndOnlyOneNotNull(Map<String, String> formValues, String... fieldNames) {
        // Count how many are not null
        List<Tuple2<String, String>> errors = new ArrayList<>();
        int count = 0;
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            if (formValues.get(fieldName) != null) {
                ++count;
            }
        }

        // Must be 1
        if (count != 1) {
            for (String fieldName : fieldNames) {
                errors.add(new Tuple2<>(fieldName, "error.oneAndOnlyOne"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validatePath(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validatePath(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validatePath(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            if (!pathValidationRegex.matcher(fieldValue).matches()) {
                errors.add(new Tuple2<>(fieldName, "error.notPath"));
            }
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateSamePassword(Map<String, String> formValues, String fieldPassword, String fieldPasswordConf) {

        List<Tuple2<String, String>> errors = new ArrayList<>();

        String password = formValues.get(fieldPassword);
        String passwordConf = formValues.get(fieldPasswordConf);

        if (!StringTools.safeEquals(password, passwordConf)) {
            errors.add(new Tuple2<>(fieldPasswordConf, "error.notSamePassword"));
        }

        return errors;
    }

    public static List<Tuple2<String, String>> validateUrl(Map<String, String> formValues) {
        Set<String> fieldNames = formValues.keySet();
        return validateUrl(formValues, fieldNames.toArray(new String[fieldNames.size()]));
    }

    public static List<Tuple2<String, String>> validateUrl(Map<String, String> formValues, String... fieldNames) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        for (String fieldName : CommonFieldHelper.getAllFieldNames(formValues, fieldNames)) {
            String fieldValue = formValues.get(fieldName);
            errors.addAll(validateUrl(fieldName, fieldValue));
        }
        return errors;
    }

    public static List<Tuple2<String, String>> validateUrl(String fieldName, String fieldValue) {
        List<Tuple2<String, String>> errors = new ArrayList<>();
        if (!Strings.isNullOrEmpty(fieldValue)) {
            try {
                new URL(fieldValue);
            } catch (MalformedURLException e) {
                errors.add(new Tuple2<>(fieldName, "error.notUrl"));
            }
        }
        return errors;
    }

    public static boolean validPath(String fieldValue) {
        if (!Strings.isNullOrEmpty(fieldValue)) {
            return pathValidationRegex.matcher(fieldValue).matches();
        }
        return true;
    }

    private CommonValidation() {
    }
}
