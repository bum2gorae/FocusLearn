import cv2
from ultralytics import YOLO
import os
import numpy as np
import json


def predict_img(frame, camera_on):
    # 현재 작업 디렉토리 가져오기
    current_dir = os.path.dirname(os.path.abspath(__file__))

    # 모델 파일 경로 생성
    model_path = os.path.join(current_dir, 'best.pt')

    # YOLO 모델 로드
    model = YOLO(model_path)
    if not camera_on:
        return b''  # camera_on이 False일 경우 빈 바이트 배열 반환

    try:
        # 이미지 바이트 배열을 NumPy 배열로 변환
        np_img = np.frombuffer(frame, np.uint8)
        img_np = cv2.imdecode(np_img, cv2.IMREAD_COLOR)
        
        # YOLO 모델 예측
        results = model.predict(img_np)
        
        for result in results:
            boxes = result.boxes
            for box in boxes:
                x1, y1, x2, y2 = box.xyxy[0]  # 바운딩 박스의 좌표
                confidence_r = float(box.conf[0])  # 신뢰도 점수
                confidence = round(confidence_r, 2)
                class_id = box.cls[0]  # 클래스 ID
                class_name = result.names[int(class_id)]  # 클래스 이름
                
                x1 = int(x1)
                x2 = int(x2)
                y1 = int(y1)
                y2 = int(y2)
                
                if class_name == 'eye':
                    cv2.rectangle(img_np, (x1, y1), (x2, y2), (0, 255, 0), 1)
                    cv2.putText(img_np, f'class:{class_name}, conf:{confidence}', (x1, y1-10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 0, 0), 1)
                elif class_name == 'iris':
                    cv2.rectangle(img_np, (x1, y1), (x2, y2), (0, 0, 255), 1)
                    cv2.putText(img_np, f'class:{class_name}, conf:{confidence}', (x1, y1-20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 1)
        
        cv2.putText(img_np, 'test', (20, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255), 1)
        result_json = json.dumps(img_np.tolist())
        return result_json
    
    except Exception as e:
        print(f"Error in predict_img: {e}")
        return b''