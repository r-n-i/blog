# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure('2') do |config|
  config.vm.box = 'ychaker/clojure'
  config.vm.box_url = 'https://atlas.hashicorp.com/ychaker/boxes/clojure'
  config.vm.network 'forwarded_port', guest: 3449, host: 7000
  config.vm.provision :shell, :path => "bootstrap.sh"

  config.vm.provider 'virtualbox' do |vb|
     vb.memory = '1024'
  end
end
