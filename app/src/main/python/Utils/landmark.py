"""
This script uses Mediapipe to perform real-time face landmark detection and annotation
on webcam feed. It detects the face landmarks and highlights the eye and iris regions.
"""
from mediapipe.tasks import python
from mediapipe.tasks.python import vision

model_path = 'Resources/face_landmarker_v2_with_blendshapes.task'

# Create FaceLandmarker Objects 
base_options = python.BaseOptions(model_asset_path=model_path)
options = vision.FaceLandmarkerOptions(base_options=base_options,
                                       output_face_blendshapes=True,
                                       output_facial_transformation_matrixes=True,
                                       num_faces=3)
detector = vision.FaceLandmarker.create_from_options(options)

def get_eye_landmarks(face_landmarks, image_width, image_height):
    left_eye_landmarks = []
    right_eye_landmarks = []
    left_iris_landmarks = []
    right_iris_landmarks = []

    # Define Face Landmark Index
    LEFT_EYE = [33, 133, 160, 159, 158, 157, 173, 144, 145, 153, 154, 155, 133]
    RIGHT_EYE = [362, 382, 381, 380, 374, 373, 390, 249, 263, 466, 388, 387, 386]
    LEFT_IRIS = [468, 469, 470, 471]
    RIGHT_IRIS = [473, 474, 475, 476]

    for idx, landmark in enumerate(face_landmarks):
        if idx in LEFT_EYE:
            left_eye_landmarks.append((int(landmark.x * image_width), int(landmark.y * image_height)))
        elif idx in RIGHT_EYE:
            right_eye_landmarks.append((int(landmark.x * image_width), int(landmark.y * image_height)))
        elif idx in LEFT_IRIS:
            left_iris_landmarks.append((int(landmark.x * image_width), int(landmark.y * image_height)))
        elif idx in RIGHT_IRIS:
            right_iris_landmarks.append((int(landmark.x * image_width), int(landmark.y * image_height)))
    
    return left_eye_landmarks, right_eye_landmarks, left_iris_landmarks, right_iris_landmarks

def get_centroid(points):
    x_coords = [point[0] for point in points]
    y_coords = [point[1] for point in points]
    centroid_x = sum(x_coords) // len(points)
    centroid_y = sum(y_coords) // len(points)
    
    return centroid_x, centroid_y