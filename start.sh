test_home=`pwd`
for i in `cat machine_list`
do
echo 'logging into '${i}
dbus-launch gnome-terminal -x bash -c "ssh -t ${i} 'cd ${test_home}/build/classes/java/main; java cs455.overlay.node.MessagingNode phoenix 50701;bash;'" &
done
