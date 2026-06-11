package com.fruitmkt.test;

import com.fruitmkt.service.ReviewService;
import org.junit.Test;

/**
 * Smoke tests cho workflow duyệt/từ chối đánh giá.
 */
public class ReviewModerationValidationTest {

    private final ReviewService reviewService = new ReviewService();

    @Test(expected = IllegalArgumentException.class)
    public void rejectInvalidModerationAction() throws Exception {
        reviewService.moderateReview(1, "unknown-action");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectInvalidReviewId() throws Exception {
        reviewService.moderateReview(0, "approve");
    }
}
