
package com.samsung.batch.model;

public record S3ObjectReference(
        String bucketName,
        String objectKey,
        String versionId,
        String sequencer
) {
}
