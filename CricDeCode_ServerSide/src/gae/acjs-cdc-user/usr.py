import webapp2
from google.appengine.ext import ndb
class usr(ndb.Model):
    batting_style = ndb.StringProperty(indexed=False)
    bowling_style = ndb.StringProperty(indexed=False)
    device_no = ndb.IntegerProperty(indexed=False)
    dob = ndb.StringProperty(indexed=False)
    fb_link = ndb.StringProperty(indexed=False)
    first_name = ndb.StringProperty(indexed=False)
    os = ndb.IntegerProperty(indexed=False)
    last_name = ndb.StringProperty(indexed=False)
    nick_name = ndb.StringProperty(indexed=False)
    role = ndb.StringProperty(indexed=False)
    user_id = ndb.StringProperty(indexed=True)
class usr_insert(webapp2.RequestHandler):
    def post(self):
        self.response.headers['Content-Type'] = 'text/plain'
        serverdbusertable_obj = usr()
        serverdbusertable_obj.batting_style = self.request.get('batting_style')
        serverdbusertable_obj.bowling_style = self.request.get('bowling_style')
        serverdbusertable_obj.device_no = int(self.request.get('device_no'))
        serverdbusertable_obj.dob = self.request.get('dob')
        serverdbusertable_obj.fb_link = self.request.get('fb_link')
        serverdbusertable_obj.first_name = self.request.get('first_name')
        serverdbusertable_obj.os = int(self.request.get('os'))
        serverdbusertable_obj.last_name = self.request.get('last_name')
        serverdbusertable_obj.nick_name = self.request.get('nick_name')
        serverdbusertable_obj.role = self.request.get('role')
        serverdbusertable_obj.user_id = self.request.get('user_id')
        obj_list = usr.query(serverdbusertable.user_id == serverdbusertable_obj.user_id).fetch()
        if(len(obj_list) == 0):
            serverdbusertable_obj.put()
            self.response.write('1 row inserted')
        else:
            self.response.write('row already exists')
application = webapp2.WSGIApplication([('/insert', usr_insert)], debug=True)