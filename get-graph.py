#!/usr/bin/python

import requests
from json import loads
from sys import argv

session = requests.Session()
session.auth = (argv[1], argv[2])

#print("From: %s_%s, until: %s_%s" % (argv[4], argv[3], argv[5], argv[3]))
response = loads(session.get(
    "https://metrics.camptocamp.com/render?" + "&".join(["%s=%s" % item for item in [
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.load.*.shortterm)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.load.*.shortterm)),95)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-idle)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.cpu.percent-idle)),5)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.memory.memory-used)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.memory.memory-used)),95)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.disk-vda.disk_ops.*)),50)"),
        ("target", "nPercentile(avg(aliasByNode(collectd.mapfish-geoportal-demo_gis_internal.disk-vda.disk_ops.*)),95)"),
        ("from", "%s_%s" % (argv[4], argv[3])),
        ("until", "%s_%s" % (argv[5], argv[3])),
        ("format", "json")
    ]]), verify=False
).text)
result = [r["datapoints"][0][0] for r in response]
print("load:\t50: %.1f,\t95: %.1f" % (result[0], result[1]))
print("CPU:\t50: %.0f, \t95: %.0f" % (100-result[2], 100-result[3]))
print("memory:\t50: %.2f,\t95: %.2f" % (result[4]/1024/1024/1024, result[5]/1024/1024/1024))
print("disk:\t50: %.1f,\t95: %.1f" % (result[6], result[7]))
