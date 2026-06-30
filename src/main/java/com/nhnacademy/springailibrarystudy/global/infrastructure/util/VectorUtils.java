package com.nhnacademy.springailibrarystudy.global.infrastructure.util;

public class VectorUtils {

    /**
     * 두 벡터 간의 코사인 유사도를 계산합니다.
     *
     * @param v1 첫 번째 벡터 (1024차원)
     * @param v2 두 번째 벡터 (1024차원)
     * @return 코사인 유사도 (-1.0 ~ 1.0)
     * @throws IllegalArgumentException 벡터 길이가 다른 경우
     */
    public static double cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException(
                    "벡터 길이가 같아야 합니다: v1=" + v1.length + ", v2=" + v2.length
            );
        }

        double dotProduct = 0.0;   // 내적
        double norm1 = 0.0;        // v1의 크기
        double norm2 = 0.0;        // v2의 크기

        // 한 번의 루프로 세 값 모두 계산
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        // 0으로 나누기 방지
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
