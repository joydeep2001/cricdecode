import webapp2
import json
import time
import urllib
import urllib2
import hashlib
from google.appengine.ext import ndb

class infisync(ndb.Model):
    autorenewing = ndb.IntegerProperty(indexed=False)
    initiation_ts_msec = ndb.IntegerProperty(indexed=False)
    order_id = ndb.StringProperty(indexed=False)
    sign = ndb.StringProperty(indexed=False)
    token = ndb.StringProperty(indexed=False)
    user_id = ndb.StringProperty(indexed=True)
    validuntil_ts_msec = ndb.IntegerProperty(indexed=False)
    not_valid = ndb.IntegerProperty(indexed=False)

class infisync_insert(webapp2.RequestHandler):

    def post(self):

        self.response.headers['Content-Type'] = 'text/plain'
        uid = self.request.get('user_id')
        handshake = self.request.get('hSAhnedk')
        times = int(handshake[3])
        handkey = handshake[:3]+handshake[4:]
        key = uid
        for i in xrange(times):
            key = hashlib.md5(key).hexdigest() 
        
        if(handkey == key): 
            obj_list = infisync.query(infisync.user_id == uid).fetch()
            if(len(obj_list) == 0):
                infisync_obj = infisync()
                infisync_obj.autorenewing = int(self.request.get('autorenewing'))
                infisync_obj.initiation_ts_msec = int(self.request.get('initiation_ts_msec'))
                infisync_obj.order_id = self.request.get('order_id')
                infisync_obj.sign = self.request.get('sign')
                infisync_obj.token = self.request.get('token')
                infisync_obj.user_id = self.request.get('user_id')
                infisync_obj.validuntil_ts_msec = int(self.request.get('validuntil_ts_msec'))
                infisync_obj.not_valid = 0
                infisync_obj.put()
                self.response.write('{"status" : 1}')
            else:
                infisync_obj = obj_list[0]
                infisync_obj.autorenewing = int(self.request.get('autorenewing'))
                infisync_obj.initiation_ts_msec = int(self.request.get('initiation_ts_msec'))
                infisync_obj.order_id = self.request.get('order_id')
                infisync_obj.sign = self.request.get('sign')
                infisync_obj.token = self.request.get('token')
                infisync_obj.user_id = self.request.get('user_id')
                infisync_obj.validuntil_ts_msec = int(self.request.get('validuntil_ts_msec'))
                infisync_obj.not_valid = 0
                infisync_obj.put()
                self.response.write('{"status" : 1}')

class infisync_retrieve(webapp2.RequestHandler):

    def post(self):
        self.response.headers['Content-Type'] = 'text/plain'
        uid = self.request.get('user_id')
        obj_list = infisync.query(infisync.user_id == uid).fetch()
        json_obj = {}
        if(len(obj_list) == 0):
            json_obj["status"] = 0
        else:
            if(obj_list[0].not_valid==1):
                json_obj["status"] = 0
            else:
                json_obj["status"] = 1
        self.response.write(json.dumps(json_obj))

class infisync_check(webapp2.RequestHandler):

    def post(self):
        self.response.headers['Content-Type'] = 'text/plain'
        uid = self.request.get('user_id')
        token = self.request.get('token')
        sign = self.request.get('sign')
        orderid = self.request.get('orderId')
        obj_list = infisync.query(infisync.user_id == uid).fetch()
        ret_status = {}
        url = "http://acjs-cdc-andro.appspot.com/retrieve_wo_json"
        values = {}
        values['user_id'] = uid
        data = urllib.urlencode(values)
        req = urllib2.Request(url, data)
        response = urllib2.urlopen(req)
        regids_str = response.read()
        if(len(obj_list) != 0):
            obj = obj_list[0]
            now = int(round(time.time()*1000))
            if now < obj.validuntil_ts_msec:
                ret_status["status"] = 1
                ret_status["reg_ids"] = regids_str
            else:                
                url = "http://acjs.azurewebsites.net/acjs/CDCSubscriptionPurchaseChk_vGAE.php"
                values = {}
                values['token'] = obj.token
                values['product_id'] = 'sub_infi_sync'
                values['reg_ids'] = regids_str                
                values['user_id'] = uid
                data = urllib.urlencode(values)
                req = urllib2.Request(url, data)
                response = urllib2.urlopen(req)
                json_str = response.read()
                json_obj = json.loads(json_str)
                if(json_obj['status'] == 1):                   
                    obj.user_id = uid
                    obj.autorenewing = json_obj["auto_ren"]
                    obj.initiation_ts_msec = json_obj["init_ts"]
                    obj.not_valid = 0
                    obj.order_id = orderid
                    obj.sign = sign
                    obj.token = token
                    obj.validuntil_ts_msec = json_obj["valid_ts"]
                    obj.put()
                    ret_status["status"] = 1
                    ret_status['reg_ids'] = regids_str
                if(json_obj['status'] == 0):
                    obj.not_valid = 1
                    obj.put()
                    ret_status["status"] = 0
                if(json_obj['status'] == 2):
                    ret_status["status"] = 2
                 
        else:
            url = "http://acjs.azurewebsites.net/acjs/CDCSubscriptionPurchaseChk_vGAE.php"
            values = {}
            values['token'] = token
            values['product_id'] = 'sub_infi_sync'
            values['reg_ids'] = regids_str                
            values['user_id'] = uid
            data = urllib.urlencode(values)
            req = urllib2.Request(url, data)
            response = urllib2.urlopen(req)
            json_str = response.read()
            json_obj = json.loads(json_str)
            if(json_obj['status'] == 1):
                obj = infisync()                 
                obj.user_id = uid
                obj.autorenewing = json_obj["auto_ren"]
                obj.initiation_ts_msec = json_obj["init_ts"]
                obj.not_valid = 0
                obj.order_id = orderid
                obj.sign = sign
                obj.token = token
                obj.validuntil_ts_msec = json_obj["valid_ts"]
                obj.put()
                ret_status["status"] = 1
                ret_status['reg_ids'] = regids_str
                if(json_obj['status'] == 0):
                    obj.not_valid = 1
                    obj.put()
                    ret_status["status"] = 0
                if(json_obj['status'] == 2):
                    ret_status["status"] = 2
                      
        self.response.write(json.dumps(ret_status))

application = webapp2.WSGIApplication([
    ('/insert', infisync_insert),('/retrieve', infisync_retrieve),('/check',infisync_check)], debug=True)
