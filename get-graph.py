#!/usr/bin/python

import requests
from json import loads
from sys import argv

session = requests.Session()
session.auth = (argv[1], argv[2])

print("From: %s_%s, until: %s_%s" % (argv[4], argv[3], argv[5], argv[3]))
response = session.get(
    "https://metrics.camptocamp.com/render?" + "&".join(["%s=%s" % item for item in [
        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.load.*.shortterm))"),
#        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.load.*.midterm))"),
#        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.load.*.longterm))"),
#        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-user))"),
#        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-system))"),
        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-idle))"),
        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.memory.memory-used))"),
        ("target", "avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.disk-vda.disk_ops.*))"),
#        ("from", "22:17_20150723"),
#        ("until", "22:47_20150723"),
        ("from", "%s_%s" % (argv[4], argv[3])),
        ("until", "%s_%s" % (argv[5], argv[3])),
        ("format", "json")
    ]]), verify=False
)
result = {}
for data in loads(response.text):
    d = sorted([e[0] for e in data["datapoints"]])
#    print("%s: 50: %f, 95: %f, 5: %f" % (data["target"], d[len(d)//2], d[len(d)*95//100], d[len(d)*5//100]))
    result[data["target"]] = {
        50: d[len(d)//2],
        95: d[len(d)*95//100],
        5: d[len(d)*5//100]
    }

#print result.keys()
r = result["averageSeries(collectd.mapfish-geoportal-demo_gis_internal.load.*.shortterm)"]
print("load: 50: %.1f, 95: %.1f" % (r[50], r[95]))
r = result["averageSeries(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-idle)"]
print("CPU: 50: %.0f, 95: %.0f" % (100-r[50], 100-r[5]))
r = result["averageSeries(collectd.mapfish-geoportal-demo_gis_internal.memory.memory-used)"]
print("memory: 50: %.2f, 95: %.2f" % (r[50]/1024/1024/1024, r[95]/1024/1024/1024))
r = result["averageSeries(collectd.mapfish-geoportal-demo_gis_internal.disk-vda.disk_ops.*)"]
print("disk: 50: %.1f, 95: %.1f" % (r[50], r[95]))

#print(result)
