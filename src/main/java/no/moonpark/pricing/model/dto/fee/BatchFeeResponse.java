package no.moonpark.pricing.model.dto.fee;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One result per request item, in the same order. Either {@code fee} or {@code error} is set.
 */
public record BatchFeeResponse(List<Result> results) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Result(int index, FeeResponse fee, String error) {

        public static Result success(int index, FeeResponse fee) {
            return new Result(index, fee, null);
        }

        public static Result failure(int index, String error) {
            return new Result(index, null, error);
        }
    }
}
