import cv2
import time
import json
import numpy as np
from Utils import process_frame as pf
from flask import Flask, request, jsonify, redirect, url_for, send_from_directory
from werkzeug.utils import secure_filename
import os

app = Flask(__name__)

@app.route('/web_test', methods=['GET'])
def online_test_for_local_laptop():
    cap = cv2.VideoCapture(0)
    while True:
        _, frame = cap.read()
        coordinates = pf.get_centroid_coords_from(frame)
        time.sleep(1)
        yield json.dumps(coordinates)

data_storage = []
request_id = 0

@app.route('/test', methods=['POST'])
def post_data():
    global request_id
    
    data = request.get_json()
    np_img = np.array(data, dtype=np.uint8)
    coordinates = pf.get_centroid_coords_from(np_img)
    
    data_storage.append(coordinates)
    request_id += 1
    
    response = {
        "request_id": request_id,
        "result": data_storage[-1],
    }
    
    return jsonify(response), 200

@app.route('/test', methods=['GET'])
def get_data():
    return jsonify({"data": data_storage[-1], "request_id": request_id}), 200

# 업로드된 파일을 저장할 디렉토리 경로 설정
UPLOAD_FOLDER = 'C:/Users/hyunj/Downloads/focuslearnvideo'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

# 허용할 파일 확장자 설정
ALLOWED_EXTENSIONS = {'mp4'}

@app.route('/test', methods = ['DELETE'])
def del_data():
    global data_storage
    request_id = 0
    data_storage.clear()
    return jsonify({"message": "Data deleted"}), 200

@app.route('/video')
def upload_form():
    return '''
    <!doctype html>
    <html lang="en">
      <head>
        <meta charset="UTF-8">
        <title>Upload an MP4 File</title>
      </head>
      <body>
        <h1>Upload MP4 File</h1>
        <form action="/upload" method="post" enctype="multipart/form-data">
          <input type="file" name="file">
          <input type="submit" value="Upload">
        </form>
        <h1>Uploaded Files</h1>
        <ul>
          {}
        </ul>
      </body>
    </html>
    '''.format('<br>'.join(f'<li><a href="/uploads/{filename}">{filename}</a></li>' for filename in os.listdir(UPLOAD_FOLDER)))



def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return 'No file part'
    file = request.files['file']
    if file.filename == '':
        return 'No selected file'
    if file and allowed_file(file.filename):
        filename = secure_filename(file.filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        return redirect(url_for('upload_form'))
    else:
        return 'Allowed file types are mp4'
    
@app.route('/uploads/<filename>')
def uploaded_file(filename):
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)

if __name__ == "__main__":
    if not os.path.exists(UPLOAD_FOLDER):
        os.makedirs(UPLOAD_FOLDER)
    # app.run(host='192.168.0.101', port=3700)
    app.run(host='192.168.45.55', port=3700)