import cv2
from ultralytics import YOLO
import os
import numpy as np
import json

# 현재 작업 디렉토리 가져오기
current_dir = os.path.dirname(os.path.abspath(__file__))

# 모델 파일 경로 생성
model_path = os.path.join(current_dir, 'best.pt')

# YOLO 모델 로드
model = YOLO(model_path)

def predict_img(frame):
    try:
        # 파이썬 리스트를 NumPy 배열로 변환
        # np_img = np.frombuffer(frame, dtype=np.uint8).reshape((height, width, 3))
        np_img = np.array(frame, dtype=np.uint8)

        # YOLO 모델 예측
        results = model.predict(np_img)

        eyes_info = {
            'eyes': [],
            'irises': []
        }
        
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
                xloc = int((x1 + x2) / 2)
                yloc = int((y1 + y2) / 2)
                
                if class_name == 'eye':
                    eyes_info['eyes'].append({'xloc': xloc, 'yloc': yloc})
                
                elif class_name == 'iris':
                    eyes_info['irises'].append({'xloc': xloc, 'yloc': yloc})

        return json.dumps(eyes_info)
    
    except Exception as e:
        print(f"Error in predict_img: {e}")
        return json.dumps({'eyes': [], 'irises': []})
