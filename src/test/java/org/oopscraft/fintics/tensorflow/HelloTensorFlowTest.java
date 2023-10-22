package org.oopscraft.fintics.tensorflow;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.tensorflow.*;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Placeholder;
import org.tensorflow.op.math.Add;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt32;

@Slf4j
public class HelloTensorFlowTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Hello TensorFlow " + TensorFlow.version());

        try (ConcreteFunction dbl = ConcreteFunction.create(HelloTensorFlowTest::dbl);
            TInt32 x = TInt32.scalarOf(10);
            Tensor dblX = dbl.call(x)) {
            System.out.println(x.getInt() + " doubled is " + ((TInt32)dblX).getInt());
        }
    }

    private static Signature dbl(Ops tf) {
        Placeholder<TInt32> x = tf.placeholder(TInt32.class);
        Add<TInt32> dblX = tf.math.add(x, x);
        return Signature.builder().input("x", x).output("dbl", dblX).build();
    }

    @Test
    void test() {
        try (Graph graph = new Graph()) {
            Ops tf = Ops.create(graph);

            // XOR 데이터
            float[] inputX = {0, 0, 1, 1};
            float[] inputY = {0, 1, 0, 1};
            float[] output = {0, 1, 1, 0};

            // 훈련 데이터 생성
//            Tensor<TFloat32> x = TFloat32.tensorOf(inputX);
//            Tensor<TFloat32> y = TFloat32.tensorOf(inputY);
//            Tensor<TFloat32> labels = TFloat32.tensorOf(output);

            // 간단한 신경망 모델 정의
            // 여기에 모델 레이어 및 파라미터 추가 가능

            // 모델 컴파일 및 훈련
            // 여기에 훈련 루프 및 옵티마이저 추가 가능

            // 예측 수행
            // 여기에 예측 코드 추가
        }

        log.info("=============== end ==================");

    }

}
