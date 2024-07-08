import cv2
import mediapipe as mp
from Utils import landmark

def get_centroid_coords_from(frame):
    # Get User's Image from webcam
    image_height, image_width, _ = frame.shape
    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb_frame)

    # Face Landmark Detection
    detection_result = landmark.detector.detect(image)

    # Eye, Iris Detection
    coordinates = {}
    if detection_result.face_landmarks:
        face_landmarks = detection_result.face_landmarks[0]
        left_eye_landmarks, right_eye_landmarks, left_iris_landmarks, right_iris_landmarks = landmark.get_eye_landmarks(face_landmarks, image_width, image_height)

        L_eye_C = landmark.get_centroid(left_eye_landmarks)
        R_eye_C = landmark.get_centroid(right_eye_landmarks)
        L_iris_C = landmark.get_centroid(left_iris_landmarks)
        R_iris_C = landmark.get_centroid(right_iris_landmarks)

        coordinates = {
                'left_eye': L_eye_C,
                'right_eye': R_eye_C,
                'left_iris': L_iris_C,
                'right_iris': R_iris_C
            }
        
        L_x1, L_x2 = logic(left_eye_landmarks, 0.3)
        R_x1, R_x2 = logic(right_eye_landmarks, 0.3)

        if (L_x1 < L_iris_C < L_x2) or (R_x1 < R_iris_C < R_x2):
            return 1
        else:
            return 0
        
        # return coordinates
    
def logic(landmarks, ratio):
    x_coords = [point[0] for point in landmarks]

    min_x = min(x_coords)
    max_x = max(x_coords)
    
    x1 = (ratio * max_x + (1 - ratio) * min_x)
    x2 = ((1 - ratio) * max_x + ratio * min_x)

    return x1, x2