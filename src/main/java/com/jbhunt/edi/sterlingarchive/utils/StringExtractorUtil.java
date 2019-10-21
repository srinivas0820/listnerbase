package com.jbhunt.edi.sterlingarchive.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class StringExtractorUtil {

    private String str;

    public StringExtractorUtil(String str) {
        this.str = str;
    }

    //returns the string between left bound and right bound, removing it (including leftBound and rightBound themselves if removeBounds is true)
    //from the original string. Returns null if there is no instance of left bound followed by * (any string, including empty) followed by right bound
    private String doExtract(String leftBound, String rightBound, boolean removeBounds) {
        int leftIndex = str.indexOf(leftBound);
        int rightIndex = str.indexOf(rightBound);
        if (leftIndex >= rightIndex) {
            return null;
        }
        String result = str.substring(leftIndex + (removeBounds ? leftBound.length() : 0),
                rightIndex + (removeBounds ? 0 : rightBound.length()));
        StringBuilder builder = new StringBuilder();
        builder.append(str.substring(0, leftIndex + (removeBounds ? 0 : leftBound.length())));
        builder.append(str.substring(rightIndex + (removeBounds ? rightBound.length() : 0)));
        str = builder.toString();
        return result;
    }

    public String extract(String leftBound, String rightBound) {
        return doExtract(leftBound, rightBound, true);
    }

    public String extractPreservingBounds(String leftBound, String rightBound) {
        return doExtract(leftBound, rightBound, false);
    }

    public String getStr() {
        return str;
    }

    public String dualExtract() {
        Optional<String> findResults;
        findResults = Optional.ofNullable(extract("<PROCESS_ID>", "</PROCESS_ID>"));
        if (findResults.isPresent()) {
            return findResults.get();
        }
        findResults = Optional.ofNullable(extract("<myWorkflowID>", "</myWorkflowID>"));
        if (findResults.isPresent()) {
            return findResults.get();
        }
        throw new IllegalStateException("ProcessData did not contain PROCESS_ID or myWorkflowID");
    }
}
