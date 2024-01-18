function _createDailyOhlcvsChart(elementId) {
    return new Chart(document.getElementById(elementId), {
        type: 'line',
        data: {
            datasets: []
        },
        options: {
            maintainAspectRatio: false,
            parsing: {
                xAxisKey: 'dateTime',
                yAxisKey: 'pctChange'
            },
            scales: {
                x: {
                    type: 'time',
                    distribution: 'linear',
                    time: {
                        unit: 'day',
                        displayFormats: {
                            hour: 'MM-dd'
                        }
                    },
                    ticks: {
                        stepSize: 3,
                        font: {
                            size: 8
                        }
                    },
                    title: {
                        display: true,
                        text: 'Daily',
                        color: '#911',
                        font: {
                            weight: 'bold',
                            lineHeight: 1.2,
                        }
                    }
                },
                y: {
                    ticks: {
                        stepSize: 0.5,
                        font: {
                            size: 8
                        },
                        callback: function(value, index, values) {
                            return value.toFixed(2) + ' %';
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + context.raw['closePrice'].toLocaleString() +
                                ' (' + context.raw['pctChange'] + '%)';
                        }
                    }
                }
            }
        }
    });
}

function _createMinuteOhlcvsChart(elementId) {
    return new Chart(document.getElementById(elementId), {
        type: 'line',
        data: {
            datasets: []
        },
        options: {
            maintainAspectRatio: false,
            parsing: {
                xAxisKey: 'dateTime',
                yAxisKey: 'pctChange'
            },
            scales: {
                x: {
                    type: 'time',
                    distribution: 'linear',
                    time: {
                        unit: 'hour',
                        displayFormats: {
                            hour: 'MM-dd HH:00'
                        }
                    },
                    ticks: {
                        stepSize: 1,
                        font: {
                            size: 8
                        }
                    },
                    title: {
                        display: true,
                        text: 'Minute',
                        color: '#911',
                        font: {
                            weight: 'bold',
                            lineHeight: 1.2,
                        }
                    }
                },
                y: {
                    ticks: {
                        stepSize: 0.5,
                        font: {
                            size: 8
                        },
                        callback: function(value, index, values) {
                            return value.toFixed(2) + ' %';
                        }
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return context.dataset.label + ': ' + context.raw['closePrice'].toLocaleString() +
                                ' (' + context.raw['pctChange'] + '%)';
                        }
                    }
                }
            }
        }
    });
}

function _createTimeSeries(dateFrom, dateTo, ohlcvs) {
    let ohlcvSeries = JSON.parse(JSON.stringify(ohlcvs)).reverse();
    let timeSeries = [];
    ohlcvSeries.forEach(ohlcvPoint => {
        let dateTime = new Date(ohlcvPoint.dateTime);
        if(dateFrom.getTime() <= dateTime.getTime() && dateTime.getTime() <= dateTo.getTime()) {
            timeSeries.push(ohlcvPoint);
        }
    });

    // fill pct change
    let basePrice;
    timeSeries.forEach(ohlcv => {
        if(!basePrice) {
            basePrice = ohlcv.openPrice;
        }
        ohlcv.pctChange = Number((ohlcv.closePrice - basePrice)/basePrice * 100).toFixed(4);
    });
    return timeSeries;
}