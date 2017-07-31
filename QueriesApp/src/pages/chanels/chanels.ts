import { Component, Injectable } from '@angular/core';
import { Http, Headers } from '@angular/http';
import { IonicPage, NavController, NavParams, AlertController } from 'ionic-angular';
import { QueriesPage } from '../queries/queries';
/**
 * Generated class for the ChanelsPage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */
@IonicPage()
@Component({
    selector: 'page-chanels',
    templateUrl: 'chanels.html'
})

@Injectable()
export class ChanelsPage {
    mode: string;
    questions: any[];
    queryAnswers: {};
    channels: any[];
    userChannels: any[];
    user: any;
    public static loadedData: boolean;

    constructor(public navCtrl: NavController, public navParams: NavParams, public http: Http, public alertCtrl: AlertController) {
        this.mode = "channels";
        this.questions = [];
        this.queryAnswers = {};
        this.channels = [];
        this.userChannels = [];
        this.user = {};
        if (this.navParams.get('user') != undefined) {
            this.user = this.navParams.get('user');
        }

        //for (var i = 0; i < 10; i++) {
        //    this.channels.push({
        //        Name: 'Channel' + i.toString()
        //    });
        //}

        //var xhttp = new XMLHttpRequest();
        //xhttp.setRequestHeader('Accept', 'text/plain');
        //xhttp.setRequestHeader('Content-Type', 'application/json');
        //xhttp.setRequestHeader('x-okapi-tenant', 'ttt');
        //xhttp.open('GET', 'http://ec2-52-15-97-36.us-east-2.compute.amazonaws.com:8084/channel', true);
        //xhttp.onreadystatechange = function () {
        //    if (this.readyState == 4 && this.status == 200) {
        //        var respChannels = this.responseText;
        //    }
        //};        

        let myHeaders: Headers = new Headers();
        myHeaders.set('Accept', 'text/plain');
        myHeaders.set('Content-Type', 'application/json');
        myHeaders.set('x-okapi-tenant', 'ttt');
        this.http.get('http://ec2-52-15-97-36.us-east-2.compute.amazonaws.com:8084/channel', {
            headers: myHeaders
        }).subscribe((resp) => {
            this.channels = JSON.parse(resp["_body"]).channel;
        });

        ChanelsPage.loadedData = true;
    }

    openChannel(channel) {
        this.navCtrl.push(QueriesPage, {
            channel: channel
        });
    }

    openNewChannelModal() {
        let newChannelAlert = this.alertCtrl.create({
            title: 'Create New Cahnnel',
            message: "Enter a name for the channel",
            inputs: [
                {
                    name: 'channel',
                    placeholder: 'Channel Name'
                },
            ],
            buttons: [
                {
                    text: 'Cancel',
                    handler: data => {
                        console.log('Cancel clicked');
                    }
                },
                {
                    text: 'Save',
                    handler: data => {
                        let navTransition = newChannelAlert.dismiss();
                        let myHeaders: Headers = new Headers();
                        myHeaders.set('Accept', 'text/plain');
                        myHeaders.set('Content-Type', 'application/json');
                        myHeaders.set('x-okapi-tenant', 'ttt');

                        let channeldata : any = {};
                        channeldata.channel_name = data.channel;
                        channeldata.members = 0;
                        channeldata.owner = "ttt";
                        channeldata.tags = [];
                        this.http.post('http://ec2-52-15-97-36.us-east-2.compute.amazonaws.com:8084/channel', channeldata, {
                            headers: myHeaders
                        });
                    }
                }
            ]
        });
        
        newChannelAlert.present();
    }

    toggleDetails(data) {
        if (data.showDetails) {
            data.showDetails = false;
            //data.icon = 'ios-add-circle-outline';
        } else {
            data.showDetails = true;
            //data.icon = 'ios-remove-circle-outline';
        }
    }

    submitVote(query, answer) {
        if (this.queryAnswers[query.question] == undefined) {
            for (var i = 0; i < query.answers.length; i++) {
                if (query.answers[i].A == answer.A) {
                    query.answers[i].Count++;
                }
            }

            query.totalVotes++;
            this.queryAnswers[query.question] = "true";
        }
    }

    ionViewDidLoad() {
        console.log('ionViewDidLoad ChanelsPage');
    }

    ngAfterViewChecked() {
        if (!ChanelsPage.loadedData) {
            let myHeaders: Headers = new Headers();
            myHeaders.set('Accept', 'text/plain');
            myHeaders.set('Content-Type', 'application/json');
            myHeaders.set('x-okapi-tenant', 'ttt');
            this.http.get('http://ec2-52-15-97-36.us-east-2.compute.amazonaws.com:8084/channeluery=channel_name=* sortBy members/sort.descending', {
                headers: myHeaders
            }).subscribe((resp) => {
                this.channels = JSON.parse(resp["_body"]).channel;
            });

            ChanelsPage.loadedData = true;
        }
    }
}