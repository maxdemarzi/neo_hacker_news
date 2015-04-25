require 'rubygems'
require 'oj'
require 'glutton_ratelimit'
require 'excon'
require 'neography'

class Client
  extend GluttonRatelimit
  @@connection = Excon.new("http://hn.algolia.com", :persistent => true)
  
  def get_stories(date)    
    response = @@connection.get(:path => "/api/v1/search_by_date?tags=story&numericFilters=created_at_i>#{date.to_time.to_i},created_at_i<#{(date +1).to_time.to_i}&hitsPerPage=1000")
    Oj.load(response.body)
  end
  
  def get_comments(story_id)
    response = @@connection.get(:path => "/api/v1/search?tags=comment,story_#{story_id}&hitsPerPage=1000")
    Oj.load(response.body)
  end
    
  rate_limit :get_stories, 4500, 3600
  rate_limit :get_comments, 4500, 3600
end

@date = Date.today
@client = Client.new
@neo = Neography::Rest.new

@last = false  
@last_story = "https://github.com/Kinoma/kinomajs"
# Get data for yesterday to the last 1000 days
for i in 1..1000

  @date = @date - 1
  puts "***************************** #{@date} **********************************"
  stories = @client.get_stories(@date)
  
    stories["hits"].each do |story|
      begin
        @last_story = story["url"] if @last_story.nil?
        @last = true if (story["url"] == @last_story)
        if (story["num_comments"] > 0 && !story["url"].empty? && @last)
          users = []
          commented = []

          puts story["url"]
          @neo.post_extension("/v1/service/story", {:id => story["objectID"], :url => story["url"]})
          @neo.post_extension("/v1/service/user", {:username => story["author"]})
          @neo.post_extension("/v1/service/user/#{story["author"]}/authored/#{story["objectID"]}")
          comments = @client.get_comments(story["objectID"])
          comments["hits"].each do |comment|
            #puts comment
            unless (comment["author"].nil? || comment["author"].empty?)
              users << comment["author"]
              commented << "/v1/service/user/#{comment["author"]}/commented/#{story["objectID"]}"
            end
          end
          
          users.uniq.each do |user|
            #puts user
            @neo.post_extension("/v1/service/user", {:username => user})
          end

          commented.each do |rel|
            #puts rel
            @neo.post_extension(rel)
          end
        end
      rescue
         puts "...........................Failed..........................." 
         next        
      end        

    end      
    

end




