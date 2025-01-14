import webapp2
import json
import hashlib
from google.appengine.ext import ndb

class ads(ndb.Model):
    order_id = ndb.StringProperty(indexed=False)
    purchasetime = ndb.IntegerProperty(indexed=False)
    sign = ndb.StringProperty(indexed=False)
    token = ndb.StringProperty(indexed=False)
    user_id = ndb.StringProperty(indexed=True)

class ads_insert(webapp2.RequestHandler):

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
            ads_obj = ads()
            ads_obj.order_id = self.request.get('order_id')
            ads_obj.purchasetime = int(self.request.get('purchasetime'))
            ads_obj.sign = self.request.get('sign')
            ads_obj.token = self.request.get('token')
            ads_obj.user_id = self.request.get('user_id')

            obj_list = ads.query(ads.user_id == ads_obj.user_id).fetch()
            if(len(obj_list) == 0):
                ads_obj.put()
            self.response.write('{"status" : 1}')

class ads_retrieve(webapp2.RequestHandler):

    def post(self):

	self.response.headers['Content-Type'] = 'text/plain'

	uid = self.request.get('user_id')
	obj_list = ads.query(ads.user_id == uid).fetch()
        if(len(obj_list) == 0):
            self.response.write('{"status" : 0}')
        else:
            self.response.write('{"status" : 1}')

application = webapp2.WSGIApplication([
    ('/insert', ads_insert),('/retrieve', ads_retrieve)
], debug=True)
