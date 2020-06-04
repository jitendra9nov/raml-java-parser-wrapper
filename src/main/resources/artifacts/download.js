const url = "http://localhost:8080";

const handleUpload = (file, type) => {
    let reader = new FileReader();
    reader.onloadend = () => {
        setFile(file);
    };
    reader.readAsDataURL(file);

    if (type === "upload") {

        const formData = new FormData();
        formData.append('file', file);

        const config = {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        };
        axios.post(url + "/stubs/upload", formData, config).then(response => {
            alert(response.data);
        }).catch(error => {
            alert("error");
        });
    } else {
        const formData = new FormData();
        formData.append('filePath', '/download/path');
        formData.append('type', 'jar');//these are query params

        const config = {
            responseType: 'arraybuffer',
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        };
        axios.post(url + "/stubs/download", formData, config).then(response => {
            var fileName = extractFileName(response.headers['content-disposition']);

            let blob = new Blob([response.data], {type: 'application/zip'});
            const downloadUrl = URL.createObjectURL(blob);
            let a = document.createElement("a");
            a.href = downloadUrl;
            a.download = fileName;
            document.body.append(a);
            a.click();
        }).catch(error => {
            alert("error");
        });
    }

}


const extractFileName = (contentDispositionValue) => {
    var fileName = "";
    if (contentDispositionValue && contentDispositionValue.indexOf('attachement') !== -1) {
        var fileNameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
        var matches = fileNameRegex.exec(contentDispositionValue);

        if (matches != null && matches[1]) {
            fileName = matches[1].replace(/['"]/g, '');
        }
    }
    return fileName;
}