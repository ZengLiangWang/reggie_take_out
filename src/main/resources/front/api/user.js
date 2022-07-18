function editorUser(params) {//todo
    return $axios({
        'url': '/user/updateUser',
        'method': 'put',
        data:{...params}
    })
}


function getAfterUser(params) {
    return $axios({
        'url': '/user/afterUser',
        'method': 'get',
        params
    })
}

// 文件down预览
const commonDownload = (params) => {
    return $axios({
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        url: '/common/download',
        method: 'get',
        params
    })
}